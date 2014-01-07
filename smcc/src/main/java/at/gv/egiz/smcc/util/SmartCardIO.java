/*
 * Copyright 2011 by Graz University of Technology, Austria
 * MOCCA has been developed by the E-Government Innovation Center EGIZ, a joint
 * initiative of the Federal Chancellery Austria and Graz University of Technology.
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 */


package at.gv.egiz.smcc.util;

import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import javax.smartcardio.CardTerminals.State;

import org.openecard.scio.osx.SunOSXPCSC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author mcentner
 */
public class SmartCardIO {

  private static final int STATE_INITIALIZED = 1;
  
  private static final int STATE_TERMINAL_FACTORY = 2;
  
  private static final int STATE_TERMINALS = 3;
  
  private final Logger log = LoggerFactory.getLogger(SmartCardIO.class);
  
  final Map<CardTerminal, Card> terminalCard_ = new HashMap<CardTerminal, Card>();
  
  int state_ = STATE_INITIALIZED;

  TerminalFactory terminalFactory_ = null;
  
  CardTerminals cardTerminals_;

  private void updateTerminalFactory() {
    TerminalFactory terminalFactory;
    try {
      String osName = System.getProperty("os.name");
      if (osName.contains("OS X")) {
        terminalFactory = TerminalFactory.getInstance("PC/SC", null, new SunOSXPCSC());
      } else {
        terminalFactory = TerminalFactory.getInstance("PC/SC", null);
      }
    } catch (NoSuchAlgorithmException e) {
      log.info("Failed to get TerminalFactory of type 'PC/SC'.", e);
      terminalFactory = TerminalFactory.getDefault();
    }
    log.debug("TerminalFactory : {}.", terminalFactory);
    if ("PC/SC".equals(terminalFactory.getType())) {
      terminalFactory_ = terminalFactory;
    }
    if(state_ < STATE_TERMINAL_FACTORY) {
      state_ = STATE_TERMINAL_FACTORY;
    }
  }

  public boolean isPCSCSupported() {
    if(state_ < STATE_TERMINAL_FACTORY) {
      updateTerminalFactory();
    }
    return terminalFactory_ != null;
  }

  private void updateCardTerminals() {
    if(terminalFactory_ != null) {
      cardTerminals_ = terminalFactory_.terminals();
    }
    log.debug("CardTerminals : {}.", cardTerminals_);
    if (state_ < STATE_TERMINALS) {
      state_ = STATE_TERMINALS;
    }
  }

  public CardTerminals getCardTerminals() {
    if(state_ < STATE_TERMINAL_FACTORY) {
      updateTerminalFactory();
    }
    if(state_ < STATE_TERMINALS) {
      updateCardTerminals();
    }
    return cardTerminals_;
  }

  public boolean isTerminalPresent() {
    CardTerminals cardTerminals = getCardTerminals();
    if (cardTerminals != null) {
      List<CardTerminal> terminals = null;
      try {
        terminals = cardTerminals.list(State.ALL);
        
        // logging
        if(log.isInfoEnabled()) {
          if (terminals == null || terminals.isEmpty()) {
            log.info("No card terminal found.");
          } else {
            StringBuffer msg = new StringBuffer();
            msg.append("Found " + terminals.size() + " card terminal(s):");
            for (CardTerminal terminal : terminals) {
              msg.append("\n  " + terminal.getName());
            }
            log.info(msg.toString());
          }
        }
        
        return terminals != null && !terminals.isEmpty();
      } catch (CardException e) {
        log.info("Failed to list card terminals.", e);
        return false;
      }
    } else {
      return false;
    }
  }

  private Map<CardTerminal, Card> updateCards() {

    // clear card references if removed
    try {
      log.trace("terminals.list(State.CARD_REMOVAL)");
      for (CardTerminal terminal : cardTerminals_.list(CardTerminals.State.CARD_REMOVAL)) {
        Card card = terminalCard_.remove(terminal);
        log.trace("card removed : {}", card);
      }
    } catch (CardException e) {
      log.debug("Failed to list terminals.", e);
    }

    // check inserted cards
    Map<CardTerminal, Card> newCards = new HashMap<CardTerminal, Card>();
    try {
      log.trace("terminals.list(State.CARD_INSERTION)");
      for (CardTerminal terminal : cardTerminals_.list(CardTerminals.State.CARD_INSERTION)) {

        Card card = null;
        try {
          log.trace("Trying to connect to card.");
          // try to connect to card
          card = terminal.connect("*");
        } catch (CardException e) {
          log.trace("Failed to connect to card.", e);
        }

        // have we seen this card before?
        if (terminalCard_.put(terminal, card) == null) {
          terminalCard_.put(terminal, card);
          newCards.put(terminal, card);
          log.trace("terminal '{}' card inserted : {}", terminal, card); 
        }
      }
    } catch (CardException e) {
      log.debug("Failed to list cards.", e);
    }
    return newCards;
    
  }

  public Map<CardTerminal, Card> getCards() {
    if(state_ < STATE_TERMINAL_FACTORY) {
      updateTerminalFactory();
    }
    if(state_ < STATE_TERMINALS) {
      updateCardTerminals();
    }
    updateCards();
    Map<CardTerminal, Card> terminalCard = new HashMap<CardTerminal, Card>();
    terminalCard.putAll(terminalCard_);
    return Collections.unmodifiableMap(terminalCard);
  }

  public Map<CardTerminal, Card> waitForInserted(int timeout) {
    if(state_ < STATE_TERMINAL_FACTORY) {
      updateTerminalFactory();
    }
    if(state_ < STATE_TERMINALS) {
      updateCardTerminals();
    }
    try {
      // just waiting for a short period of time to allow for abort
      cardTerminals_.waitForChange(timeout);
    } catch (CardException e) {
      log.debug("CardTerminals.waitForChange({}) failed.", timeout, e);
    }
    Map<CardTerminal, Card> newCards = new HashMap<CardTerminal, Card>();
    newCards.putAll(updateCards());
    return Collections.unmodifiableMap(newCards);
  }
}  
