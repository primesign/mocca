/*
* Copyright 2008 Federal Chancellery Austria and
* Graz University of Technology
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package at.gv.egiz.smcc.util;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author mcentner
 */
public class SmartCardIO {

  private static final int STATE_INITIALIZED = 1;
  
  private static final int STATE_TERMINAL_FACTORY = 2;
  
  private static final int STATE_TERMINALS = 3;
  
  private static Log log = LogFactory.getLog(SmartCardIO.class);
  
  final Map<CardTerminal, Card> terminalCard_ = new HashMap<CardTerminal, Card>();
  
  int state_ = STATE_INITIALIZED;

  TerminalFactory terminalFactory_ = null;
  
  CardTerminals cardTerminals_;

  private void updateTerminalFactory() {
    TerminalFactory terminalFactory = TerminalFactory.getDefault();
    log.debug("TerminalFactory : " + terminalFactory);
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
    log.debug("CardTerminals : " + cardTerminals_);
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
        log.trace("card removed : " + card);
      }
    } catch (CardException e) {
      log.debug(e);
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
          log.trace("terminal '" + terminal + "' card inserted : " + card); 
        }
      }
    } catch (CardException e) {
      log.debug(e);
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
      log.debug("CardTerminals.waitForChange(" + timeout + ") failed.", e);
    }
    Map<CardTerminal, Card> newCards = new HashMap<CardTerminal, Card>();
    newCards.putAll(updateCards());
    return Collections.unmodifiableMap(newCards);
  }
}  