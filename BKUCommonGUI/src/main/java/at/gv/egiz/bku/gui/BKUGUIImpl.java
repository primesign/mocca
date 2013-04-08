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



package at.gv.egiz.bku.gui;

import at.gv.egiz.bku.gui.viewer.FontProviderException;
import at.gv.egiz.bku.gui.viewer.FontProvider;
import at.gv.egiz.bku.gui.viewer.SecureViewerSaveDialog;
import at.gv.egiz.smcc.PinInfo;
import at.gv.egiz.stal.HashDataInput;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author clemens
 */
public class BKUGUIImpl implements BKUGUIFacade {

	private final Logger log = LoggerFactory.getLogger(BKUGUIImpl.class);

	protected enum PinLabelPosition {
		LEFT, ABOVE
	}

	protected Component primaryFocusHolder;
	protected SecureViewerDialog secureViewer;

	protected HelpListener helpListener;
	protected FontProvider fontProvider;

	protected Container contentPane;
  protected WindowCloseAdapter windowCloseAdapter;
	protected ResourceBundle messages;
	/** left and right side main panels */
	protected JPanel iconPanel;
	protected JPanel contentPanel;
	/** right side content panels and layouts */
	protected JPanel headerPanel;
	protected JPanel mainPanel;
	protected JPanel buttonPanel;
	/** right side fixed labels */
	protected JLabel titleLabel;
	protected JLabel msgTitleLabel;
	protected JLabel helpLabel;
	/** remember the pinfield to return to worker */
	protected JPasswordField pinField;
	protected Document pinpadPIN;

	protected JButton okButton;
	protected JButton backButton;
	protected JButton enterPINButton;
	protected final JButton cancelButton;
	protected JLabel infoLabel;
	protected final JLabel pinsizeLabel;
	protected final JLabel signPinLabel;
	protected final JButton signButton;
	protected JLabel cardPinLabel;
	protected JLabel pinLabel;
	protected JPasswordField pinpadPINField;
	protected JLabel msgLabel;
	protected boolean showMessageOKButton;
	protected JLabel refIdLabel;
	protected JScrollPane hashDataScrollPane;
	protected JTable hashDataTable;
	protected HyperlinkRenderer hyperlinkRenderer;
	protected int baseTableRowHeight;

	protected FocusBorder sigDataFocusBorder;
	protected FocusBorder helpFocusBorder;

	protected Method methodToRunAtResize;

	protected int buttonSize;
	protected int baseButtonSize;
	protected Integer baseWidth;
	protected Integer baseHeight;
	protected int baseFontSize;

	/** gui style config (default 'simple') */
	protected boolean renderHeaderPanel = false;
	protected boolean renderIconPanel = false;
	protected boolean renderCancelButton = false;
	protected boolean shortText = false;
	protected PinLabelPosition pinLabelPos = PinLabelPosition.LEFT;
	protected boolean renderRefId = false;
	protected boolean useFocusTraversalPolicy = false;
	
//	protected HashDataInput storedSelection;
	protected List<HashDataInput> signedReferences;
	protected Integer referenceIndex;
	private at.gv.egiz.bku.gui.BKUGUIImpl.SignedReferencesSelectionListener.SignedReferencesListDisplayer storedBackToListListener;

	/**
	 * set contentPane init message bundle configure the style register the help
	 * listener create GUI (on event-dispatching thread)
	 * 
	 * @param contentPane
	 * @param locale
	 * @param guiStyle
	 * @param background
	 * @param helpListener
	 */
	public BKUGUIImpl(Container contentPane, Locale locale, Style guiStyle,
			URL background, FontProvider fontProvider,
			HelpListener helpListener) {
		this.contentPane = contentPane;
		Window w = SwingUtilities.getWindowAncestor(contentPane);
    if (w != null && w instanceof JFrame) {
        this.windowCloseAdapter = new WindowCloseAdapter();
        ((JFrame) w).addWindowListener(windowCloseAdapter);
    }
		
		loadMessageBundle(locale);

		cancelButton = new JButton();
		infoLabel = new JLabel();
		cardPinLabel = new JLabel();
		pinsizeLabel = new JLabel();
		signPinLabel = new JLabel();
		signButton = new JButton();
		pinLabel = new JLabel();
		pinpadPINField = new JPasswordField();
		msgLabel = new JLabel();
		showMessageOKButton = false;

		this.baseFontSize = new JLabel().getFont().getSize();
		this.baseTableRowHeight = new JTable().getRowHeight();
		
		if (guiStyle == Style.advanced) {
			renderHeaderPanel = true;
			renderIconPanel = false;
			renderCancelButton = true;
			renderRefId = true;
			useFocusTraversalPolicy = true;
		} else if (guiStyle == Style.tiny) {
			shortText = true;
			pinLabelPos = PinLabelPosition.ABOVE;
		}

		// ensure that buttons can be fired with enter key too
		UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);

		this.fontProvider = fontProvider;
		this.helpListener = helpListener;
		createGUI(background);

	}

	private void createGUI(final URL background) {

		try {

			log.debug("Scheduling gui initialization.");

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {

					log.debug("[{}] Initializing gui.", Thread.currentThread().getName());

					if (renderIconPanel) {
						initIconPanel(background);
						initContentPanel(null);
					} else {
						initContentPanel(background);
					}

					contentPanel.addComponentListener(new ComponentAdapter() {

						@Override
						public void componentResized(ComponentEvent e) {

							log.debug("Component resize detected.");

							resize();
						}

					});

					GroupLayout layout = new GroupLayout(contentPane);
					contentPane.setLayout(layout);

					if (renderIconPanel) {
						layout
								.setHorizontalGroup(layout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(iconPanel,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(contentPanel,
												GroupLayout.DEFAULT_SIZE,
												GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addContainerGap());
						layout
								.setVerticalGroup(layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												layout
														.createParallelGroup(
																GroupLayout.Alignment.LEADING)
														.addComponent(
																iconPanel,
																GroupLayout.Alignment.TRAILING,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(
																contentPanel,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE))
										.addContainerGap());
					} else {
						layout.setHorizontalGroup(layout
								.createSequentialGroup()
								// left border
								.addContainerGap().addComponent(contentPanel)
								.addContainerGap());
						layout.setVerticalGroup(layout.createSequentialGroup()
								.addContainerGap().addComponent(contentPanel)
								.addContainerGap());
					}
				}
			});

		} catch (Exception ex) {
			throw new RuntimeException("Failed to init GUI: " + ex.getMessage());
		}
	}

	protected void initIconPanel(URL background) {
		if (background == null) {
			background = getClass().getResource(DEFAULT_ICON);
		}
		if ("file".equals(background.getProtocol())) {
            log.warn("file:// background images not permitted: {}, "
                + "loading default background", background);
			background = getClass().getResource(DEFAULT_ICON);
		}
		log.debug("Loading icon panel background {}.", background);

		iconPanel = new JPanel();
		JLabel iconLabel = new JLabel();
		iconLabel.setIcon(new ImageIcon(background));

		GroupLayout iconPanelLayout = new GroupLayout(iconPanel);
		iconPanel.setLayout(iconPanelLayout);
		iconPanelLayout.setHorizontalGroup(iconPanelLayout
				.createSequentialGroup().addComponent(iconLabel,
						GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE));
		iconPanelLayout.setVerticalGroup(iconPanelLayout
				.createSequentialGroup().addComponent(iconLabel,
						GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE));
	}

	protected void initContentPanel(URL background) {

		if (background == null) {
			log.debug("No background image set.");
			// contentPanel = new
			// ImagePanel(getClass().getResource(DEFAULT_BACKGROUND));
			contentPanel = new JPanel();
		} else if ("file".equals(background.getProtocol())) {
			log.warn("file:// background images not permitted: {}.", background);
			contentPanel = new JPanel();
		} else {
			log.debug("Loading background {}.", background);
			contentPanel = new ImagePanel(background);
		}
		contentPanel.setOpaque(false);
		mainPanel = new JPanel();
		mainPanel.setOpaque(false);
		buttonPanel = new JPanel();
		buttonPanel.setOpaque(false);

		okButton = new JButton();
		backButton = new JButton();
		enterPINButton = new JButton();

		sigDataFocusBorder = new FocusBorder(HYPERLINK_COLOR);
		helpFocusBorder = new FocusBorder(HELP_COLOR);

		if (helpListener.implementsListener()) {
			helpLabel = new JLabel();
			helpLabel.setIcon(new ImageIcon(getClass().getResource(HELP_IMG)));
			helpLabel.getAccessibleContext().setAccessibleName(
					getMessage(ALT_HELP));
			helpLabel.setFocusable(true);
			helpLabel.addMouseListener(helpListener);
			helpLabel.addKeyListener(helpListener);
			helpLabel.addFocusListener(new FocusAdapter() {

				@Override
				public void focusGained(FocusEvent e) {

					log.debug("Help label obtained focus.");
					updateHelpLabelIcon();
				}

				@Override
				public void focusLost(FocusEvent e) {

					updateHelpLabelIcon();
				}

			});
			helpLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}

		buttonSize = initButtonSize();
		baseButtonSize = buttonSize;

		titleLabel = new JLabel();
		msgTitleLabel = new JLabel();

		if (renderHeaderPanel) {
			headerPanel = new JPanel();
			headerPanel.setOpaque(false);

			titleLabel.setFocusable(true);
			titleLabel.setFont(titleLabel.getFont().deriveFont(
					titleLabel.getFont().getStyle() | java.awt.Font.BOLD,
					titleLabel.getFont().getSize() + 2));

			GroupLayout headerPanelLayout = new GroupLayout(headerPanel);
			headerPanel.setLayout(headerPanelLayout);

			GroupLayout.SequentialGroup horizontalHeader = headerPanelLayout
					.createSequentialGroup().addComponent(titleLabel, 0,
							GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE);

			GroupLayout.ParallelGroup verticalHeader = headerPanelLayout
					.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(titleLabel, 0, GroupLayout.PREFERRED_SIZE,
							Short.MAX_VALUE);

			if (helpListener.implementsListener()) {
				horizontalHeader.addPreferredGap(
						LayoutStyle.ComponentPlacement.UNRELATED, 0,
						Short.MAX_VALUE).addComponent(helpLabel);
				verticalHeader.addComponent(helpLabel);
			}

			headerPanelLayout.setHorizontalGroup(horizontalHeader);
			headerPanelLayout.setVerticalGroup(verticalHeader);
		}

		GroupLayout contentPanelLayout = new GroupLayout(contentPanel);
		contentPanel.setLayout(contentPanelLayout);

		// align header, main and button to the right
		GroupLayout.ParallelGroup horizontalContent = contentPanelLayout
				.createParallelGroup(GroupLayout.Alignment.TRAILING); // LEADING);
		GroupLayout.SequentialGroup verticalContent = contentPanelLayout
				.createSequentialGroup();

		if (renderHeaderPanel) {
			horizontalContent.addComponent(headerPanel, 0,
					GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
			verticalContent.addComponent(headerPanel, 0,
					GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);

		}
		horizontalContent.addComponent(mainPanel, 0, GroupLayout.DEFAULT_SIZE,
				Short.MAX_VALUE).addComponent(buttonPanel, 0,
				GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE); // Short.MAX_VALUE);
		verticalContent.addComponent(mainPanel, 0, GroupLayout.DEFAULT_SIZE,
				Short.MAX_VALUE).addPreferredGap(
				LayoutStyle.ComponentPlacement.UNRELATED).addComponent(
				buttonPanel, 0, GroupLayout.DEFAULT_SIZE,
				GroupLayout.PREFERRED_SIZE);

		contentPanelLayout.setHorizontalGroup(horizontalContent); // Outer);
		contentPanelLayout.setVerticalGroup(verticalContent);

	}

	/**
	 * BKUWorker inits signaturecard with locale
	 * 
	 * @return
	 */
	@Override
	public Locale getLocale() {
		return messages.getLocale();
	}

	/**
	 * to be overridden by subclasses providing additional resource messages
	 * 
	 * @param key
	 * @return
	 */
	protected String getMessage(String key) {
		return messages.getString(key);
	}

	/**
	 * to be overridden by subclasses providing additional resource messages
	 * 
	 * @param key
	 * @return
	 */
	protected boolean hasMessage(String key) {
		return messages.containsKey(key);
	}

  @Override
  public void showVerifyPINDialog(final PinInfo pinSpec, final int numRetries,
          final ActionListener okListener, final String okCommand,
          final ActionListener cancelListener, final String cancelCommand) {

    log.debug("Scheduling verify pin dialog.");

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

                log.debug("[{}] Show verify pin dialog.", Thread.currentThread()
                    .getName());

				mainPanel.removeAll();
				buttonPanel.removeAll();

				if (renderHeaderPanel) {
					if (numRetries < 0) {
						String verifyTitle = getMessage(TITLE_VERIFY_PIN);
						titleLabel.setText(MessageFormat.format(verifyTitle,
								new Object[] { pinSpec.getLocalizedName() }));
					} else {
						titleLabel.setText(getMessage(TITLE_RETRY));
					}
				}

				okButton.setFont(okButton.getFont().deriveFont(
						okButton.getFont().getStyle() & ~java.awt.Font.BOLD));
				okButton.setText(getMessage(BUTTON_OK));
				okButton.setEnabled(pinSpec.getMinLength() <= 0);
				okButton.setActionCommand(okCommand);
				okButton.addActionListener(okListener);

				cardPinLabel.setFont(cardPinLabel.getFont()
						.deriveFont(
								cardPinLabel.getFont().getStyle()
										& ~java.awt.Font.BOLD));
				String pinLabel = getMessage(LABEL_PIN);
				cardPinLabel.setText(MessageFormat.format(pinLabel,
						new Object[] { pinSpec.getLocalizedName() }));

				pinField = new JPasswordField();
				pinField.setText("");
				pinField.setName("PINField");
				pinField.setDocument(new PINDocument(pinSpec.getMinLength(),
						pinSpec.getMaxLength(), pinSpec.getRegexpPattern(),
						okButton));
				pinField.setActionCommand(okCommand);
				pinField.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (pinField.getPassword().length >= pinSpec
								.getMinLength()) {
							okListener.actionPerformed(e);
						}
					}
				});

				infoLabel = new JLabel();
				if (numRetries < 0) {
					infoLabel.setFont(infoLabel.getFont().deriveFont(
							infoLabel.getFont().getStyle()
									& ~java.awt.Font.BOLD));
					String infoPattern = getMessage(MESSAGE_ENTERPIN);
					if (shortText) {
						infoLabel.setText(MessageFormat.format(infoPattern,
								new Object[] { "PIN" }));
					} else {
						infoLabel.setText(MessageFormat.format(infoPattern,
								new Object[] { pinSpec.getLocalizedName() }));
					}
					helpListener.setHelpTopic(HELP_VERIFY_PIN);
				} else {
					String retryPattern;
					if (numRetries < 2) {
						retryPattern = getMessage(MESSAGE_LAST_RETRY);
					} else {
						retryPattern = getMessage(MESSAGE_RETRIES);
					}
					infoLabel.setFont(infoLabel.getFont()
							.deriveFont(
									infoLabel.getFont().getStyle()
											| java.awt.Font.BOLD));
					infoLabel.setText(MessageFormat.format(retryPattern,
							new Object[] { String.valueOf(numRetries) }));
					infoLabel.setForeground(ERROR_COLOR);
					helpListener.setHelpTopic(HELP_RETRY);
				}

				pinsizeLabel.setFont(pinsizeLabel.getFont()
						.deriveFont(
								pinsizeLabel.getFont().getStyle()
										& ~java.awt.Font.BOLD,
								pinsizeLabel.getFont().getSize() - 2));
				pinsizeLabel.setText(MessageFormat
						.format(getMessage(LABEL_PINSIZE), pinSpec
								.getLocalizedLength()));

				pinField.getAccessibleContext().setAccessibleDescription(
						cardPinLabel.getText() + pinsizeLabel.getText());

				GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
				mainPanel.setLayout(mainPanelLayout);

				GroupLayout.SequentialGroup infoHorizontal = mainPanelLayout
						.createSequentialGroup().addComponent(infoLabel);
				GroupLayout.ParallelGroup infoVertical = mainPanelLayout
						.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(infoLabel);

				if (!renderHeaderPanel) {

					if (helpListener.implementsListener()) {
						infoHorizontal.addPreferredGap(
								LayoutStyle.ComponentPlacement.UNRELATED, 0,
								Short.MAX_VALUE).addComponent(helpLabel);
						infoVertical.addComponent(helpLabel);
					}
				}

				// align pinfield and pinsize to the right
				GroupLayout.ParallelGroup pinHorizontal = mainPanelLayout
						.createParallelGroup(GroupLayout.Alignment.TRAILING);
				GroupLayout.Group pinVertical;

				if (pinLabelPos == PinLabelPosition.ABOVE) {
					pinHorizontal.addGroup(
							mainPanelLayout.createParallelGroup(
									GroupLayout.Alignment.LEADING)
									.addComponent(cardPinLabel,
											GroupLayout.PREFERRED_SIZE,
											GroupLayout.DEFAULT_SIZE,
											GroupLayout.PREFERRED_SIZE)
									.addComponent(pinField,
											GroupLayout.PREFERRED_SIZE,
											GroupLayout.DEFAULT_SIZE,
											Short.MAX_VALUE)).addComponent(
							pinsizeLabel, GroupLayout.PREFERRED_SIZE,
							GroupLayout.DEFAULT_SIZE,
							GroupLayout.PREFERRED_SIZE);
					pinVertical = mainPanelLayout.createSequentialGroup()
							.addComponent(cardPinLabel).addPreferredGap(
									LayoutStyle.ComponentPlacement.RELATED)
							.addComponent(pinField, GroupLayout.PREFERRED_SIZE,
									GroupLayout.DEFAULT_SIZE,
									GroupLayout.PREFERRED_SIZE);
				} else {
					pinHorizontal
							.addGroup(
									mainPanelLayout
											.createSequentialGroup()
											.addComponent(cardPinLabel,
													GroupLayout.PREFERRED_SIZE,
													GroupLayout.DEFAULT_SIZE,
													GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(
													LayoutStyle.ComponentPlacement.RELATED)
											.addComponent(pinField,
													GroupLayout.PREFERRED_SIZE,
													GroupLayout.DEFAULT_SIZE,
													Short.MAX_VALUE))
							.addComponent(pinsizeLabel,
									GroupLayout.PREFERRED_SIZE,
									GroupLayout.DEFAULT_SIZE,
									GroupLayout.PREFERRED_SIZE);
					pinVertical = mainPanelLayout.createParallelGroup(
							GroupLayout.Alignment.BASELINE).addComponent(
							cardPinLabel).addComponent(pinField);
				}

				mainPanelLayout.setHorizontalGroup(mainPanelLayout
						.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(infoHorizontal).addGroup(pinHorizontal));

				mainPanelLayout
						.setVerticalGroup(mainPanelLayout
								.createSequentialGroup().addGroup(infoVertical)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(pinVertical).addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(pinsizeLabel));

				if (renderCancelButton) {
					cancelButton.setFont(cancelButton.getFont().deriveFont(
							cancelButton.getFont().getStyle()
									& ~java.awt.Font.BOLD));
					cancelButton.setText(getMessage(BUTTON_CANCEL));
					cancelButton.setActionCommand(cancelCommand);
					cancelButton.addActionListener(cancelListener);
				}

				renderVerifyPINDialogueButtonPanel();

				updateMethodToRunAtResize("at.gv.egiz.bku.gui.BKUGUIImpl",
						"renderVerifyPINDialogueButtonPanel");

        if (windowCloseAdapter != null) {
          windowCloseAdapter.registerListener(cancelListener, cancelCommand);
        }

				primaryFocusHolder = pinField;

				pinField.requestFocus();
				contentPanel.validate();

				resize();

			}
		});
	}

	@SuppressWarnings("unchecked")
	protected void updateMethodToRunAtResize(String className, String methodName) {

		try {
			Class<at.gv.egiz.bku.gui.BKUGUIImpl> thisClass = (Class<at.gv.egiz.bku.gui.BKUGUIImpl>) Class
					.forName(className);
			Method m = thisClass.getMethod(methodName);
			methodToRunAtResize = m;
		} catch (SecurityException e1) {
			log.error("Unable to store rendering method.", e1);
		} catch (NoSuchMethodException e1) {
			log.error("Unable to store rendering method.", e1);
		} catch (ClassNotFoundException e) {
			log.error("Unable to store rendering method.", e);
		}

	}

	public void renderVerifyPINDialogueButtonPanel() {

		GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
		buttonPanel.setLayout(buttonPanelLayout);

		GroupLayout.SequentialGroup buttonHorizontal = buttonPanelLayout
				.createSequentialGroup().addComponent(okButton,
						GroupLayout.PREFERRED_SIZE, buttonSize,
						GroupLayout.PREFERRED_SIZE);
		GroupLayout.Group buttonVertical;

		if (renderCancelButton) {

			buttonHorizontal.addPreferredGap(
					LayoutStyle.ComponentPlacement.RELATED).addComponent(
					cancelButton, GroupLayout.PREFERRED_SIZE, buttonSize,
					GroupLayout.PREFERRED_SIZE);
			buttonVertical = buttonPanelLayout.createParallelGroup(
					GroupLayout.Alignment.BASELINE).addComponent(okButton)
					.addComponent(cancelButton);
		} else {
			buttonVertical = buttonPanelLayout.createSequentialGroup()
					.addComponent(okButton);
		}

		buttonPanelLayout.setHorizontalGroup(buttonHorizontal);
		buttonPanelLayout.setVerticalGroup(buttonVertical);

	}

    @Override
  public void showEnterPINDirect(PinInfo pinSpec, int retries) {
		if (retries < 0) {
			showMessageDialog(TITLE_VERIFY_PINPAD,
					MESSAGE_ENTERPIN_PINPAD_DIRECT, new Object[] {
							pinSpec.getLocalizedName(),
							pinSpec.getLocalizedLength() });
		} else {
			showMessageDialog(TITLE_RETRY, MESSAGE_RETRIES,
					new Object[] { String.valueOf(retries) });
		}
	}

  @Override
  public void showEnterPIN(final PinInfo pinSpec, final int retries) {
		showEnterPIN(pinSpec, retries, TITLE_VERIFY_PINPAD,
				MESSAGE_ENTERPIN_PINPAD, null);
	}

  protected void showEnterPIN(final PinInfo pinSpec, final int retries,
      final String titleKey, final String messageKey,
      final Object[] messageParams) {
    log.debug("Scheduling pinpad dialog.");

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				log.debug("[{}] show pinpad dialog.", Thread.currentThread().getName());

				mainPanel.removeAll();
				buttonPanel.removeAll();

				if (renderHeaderPanel) {
					if (retries < 0) {
						titleLabel.setText(getMessage(titleKey));
					} else {
						titleLabel.setText(getMessage(TITLE_RETRY));
					}
				}

				infoLabel = new JLabel();
				if (retries < 0) {
					infoLabel.setFont(infoLabel.getFont().deriveFont(
							infoLabel.getFont().getStyle()
									& ~java.awt.Font.BOLD));
					infoLabel.setText(MessageFormat.format(
							getMessage(messageKey), messageParams));
					helpListener.setHelpTopic(HELP_PINPAD);
				} else {
					String retryPattern;
					if (retries == 1) {
						retryPattern = getMessage(MESSAGE_LAST_RETRY);
					} else {
						retryPattern = getMessage(MESSAGE_RETRIES);
					}
					infoLabel.setText(MessageFormat.format(retryPattern,
							new Object[] { String.valueOf(retries) }));
					infoLabel.getAccessibleContext().setAccessibleName(
							infoLabel.getText());
					infoLabel.setFont(infoLabel.getFont()
							.deriveFont(
									infoLabel.getFont().getStyle()
											| java.awt.Font.BOLD));
					infoLabel.setForeground(ERROR_COLOR);
					helpListener.setHelpTopic(HELP_RETRY);
				}

				pinLabel.setFont(pinLabel.getFont().deriveFont(
						pinLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
				String pinName = getMessage(LABEL_PIN);
				pinLabel.setText(MessageFormat.format(pinName,
						new Object[] { pinSpec.getLocalizedName() }));

				pinpadPINField.setText("");
				pinpadPINField.setEnabled(false);
				pinpadPIN = pinpadPINField.getDocument();

				pinsizeLabel.setFont(pinsizeLabel.getFont()
						.deriveFont(
								pinsizeLabel.getFont().getStyle()
										& ~java.awt.Font.BOLD,
								pinsizeLabel.getFont().getSize() - 2));
				pinsizeLabel.setText(MessageFormat
						.format(getMessage(LABEL_PINSIZE), pinSpec
								.getLocalizedLength()));

				GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
				mainPanel.setLayout(mainPanelLayout);

				GroupLayout.SequentialGroup infoHorizontal = mainPanelLayout
						.createSequentialGroup().addComponent(infoLabel);
				GroupLayout.ParallelGroup infoVertical = mainPanelLayout
						.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(infoLabel);

				if (!renderHeaderPanel) {

					if (helpListener.implementsListener()) {
						infoHorizontal.addPreferredGap(
								LayoutStyle.ComponentPlacement.UNRELATED, 0,
								Short.MAX_VALUE).addComponent(helpLabel);
						infoVertical.addComponent(helpLabel);
					}

				}

				// align pinfield and pinsize to the right
				GroupLayout.Group pinHorizontal = mainPanelLayout
						.createParallelGroup(GroupLayout.Alignment.TRAILING);
				GroupLayout.SequentialGroup pinVertical = mainPanelLayout
						.createSequentialGroup();

				if (pinLabelPos == PinLabelPosition.ABOVE) {
					pinHorizontal.addGroup(
							mainPanelLayout.createParallelGroup(
									GroupLayout.Alignment.LEADING)
									.addComponent(pinLabel,
											GroupLayout.PREFERRED_SIZE,
											GroupLayout.DEFAULT_SIZE,
											GroupLayout.PREFERRED_SIZE)
									.addComponent(pinpadPINField,
											GroupLayout.PREFERRED_SIZE,
											GroupLayout.DEFAULT_SIZE,
											Short.MAX_VALUE)).addComponent(
							pinsizeLabel, GroupLayout.PREFERRED_SIZE,
							GroupLayout.DEFAULT_SIZE,
							GroupLayout.PREFERRED_SIZE);
					pinVertical.addComponent(pinLabel).addPreferredGap(
							LayoutStyle.ComponentPlacement.RELATED)
							.addComponent(pinpadPINField,
									GroupLayout.PREFERRED_SIZE,
									GroupLayout.DEFAULT_SIZE,
									GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(
									LayoutStyle.ComponentPlacement.RELATED)
							.addComponent(pinsizeLabel);
				} else { // PinLabelPosition.LEFT
					pinHorizontal
							.addGroup(
									mainPanelLayout
											.createSequentialGroup()
											.addComponent(pinLabel,
													GroupLayout.PREFERRED_SIZE,
													GroupLayout.DEFAULT_SIZE,
													GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(
													LayoutStyle.ComponentPlacement.RELATED)
											.addComponent(pinpadPINField,
													GroupLayout.PREFERRED_SIZE,
													GroupLayout.DEFAULT_SIZE,
													Short.MAX_VALUE))
							.addComponent(pinsizeLabel,
									GroupLayout.PREFERRED_SIZE,
									GroupLayout.DEFAULT_SIZE,
									GroupLayout.PREFERRED_SIZE);
					pinVertical.addGroup(
							mainPanelLayout.createParallelGroup(
									GroupLayout.Alignment.BASELINE)
									.addComponent(pinLabel).addComponent(
											pinpadPINField)).addPreferredGap(
							LayoutStyle.ComponentPlacement.RELATED)
							.addComponent(pinsizeLabel);
				}

				mainPanelLayout.setHorizontalGroup(mainPanelLayout
						.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(infoHorizontal).addGroup(pinHorizontal));

				mainPanelLayout
						.setVerticalGroup(mainPanelLayout
								.createSequentialGroup().addGroup(infoVertical)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(pinVertical));

				infoLabel.setFocusable(true);

				String accessibleData = cutOffHTMLTags(infoLabel.getText())
						+ cutOffHTMLTags(pinLabel.getText())
						+ cutOffHTMLTags(pinsizeLabel.getText());

				infoLabel.getAccessibleContext().setAccessibleName(
						accessibleData);
				infoLabel.getAccessibleContext().setAccessibleDescription(
						accessibleData);

				primaryFocusHolder = infoLabel;

				// delete potentially stored method to be run as nothing has to
				// be re-rendered
				methodToRunAtResize = null;

				infoLabel.requestFocus();

				contentPanel.validate();

				resize();
			}
		});
	}

	// simple utility method to retrieve plain text from HTML
	protected String cutOffHTMLTags(String str) {

		char[] arr = str.toCharArray();
		StringBuffer result = new StringBuffer();
		boolean inTag = false;

		for (int i = 0; i < arr.length; i++) {

			char c = arr[i];

			if (c == '<') {
				inTag = true;
			}

			if (!inTag) {

				result.append(c);
			}

			if (c == '>') {

				inTag = false;
			}
		}

		return result.toString();
	}

  @Override
  public void showSignatureDataDialog(PinInfo spec,
          final ActionListener enterPINListener, final String enterPINCommand,
          final ActionListener cancelListener, final String cancelCommand,
          final ActionListener hashdataListener, final String hashdataCommand) {

		log.debug("Scheduling signature-data dialog.");

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				log.debug("[{}] show signature-data dialog.", 
				    Thread.currentThread().getName());

				mainPanel.removeAll();
				buttonPanel.removeAll();

				// specify policy to ensure correct focus traversal
				if (useFocusTraversalPolicy) {

					contentPanel.setFocusCycleRoot(true);
					contentPanel
							.setFocusTraversalPolicy(new AdvancedShowSigDataGUIFocusTraversalPolicy());
				}

				if (renderHeaderPanel) {
					titleLabel.setText(getMessage(TITLE_SIGNATURE_DATA));
				}

				infoLabel = new JLabel();
				infoLabel.setFont(infoLabel.getFont().deriveFont(
						infoLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
				if (shortText) {
					infoLabel.setText(getMessage(MESSAGE_HASHDATALINK_TINY));
				} else {
					infoLabel.setText(getMessage(MESSAGE_HASHDATALINK));
				}
				infoLabel.getAccessibleContext().setAccessibleName(
						infoLabel.getText());
				infoLabel.setFocusable(true);

				infoLabel.setToolTipText(getMessage(SIGDATA_TOOLTIPTEXT));
				infoLabel.getAccessibleContext().setAccessibleDescription(
						getMessage(SIGDATA_TOOLTIPTEXT));
				infoLabel.getAccessibleContext().setAccessibleName(
						getMessage(SIGDATA_TOOLTIPTEXT));

				infoLabel.setCursor(Cursor
						.getPredefinedCursor(Cursor.HAND_CURSOR));
				infoLabel.setForeground(HYPERLINK_COLOR);
				infoLabel.addMouseListener(new MouseAdapter() {

					@Override
					public void mouseClicked(MouseEvent me) {
						ActionEvent e = new ActionEvent(this,
								ActionEvent.ACTION_PERFORMED, hashdataCommand);
						hashdataListener.actionPerformed(e);
					}
				});

				infoLabel.addKeyListener(new KeyAdapter() {

					@Override
					public void keyPressed(KeyEvent e) {

						if (e.getKeyCode() == KeyEvent.VK_ENTER) {
							ActionEvent e1 = new ActionEvent(this,
									ActionEvent.ACTION_PERFORMED,
									hashdataCommand);
							hashdataListener.actionPerformed(e1);
						}
					}

				});

				infoLabel.addFocusListener(new FocusAdapter() {

					@Override
					public void focusGained(FocusEvent e) {

						infoLabel.setBorder(sigDataFocusBorder);
					}

					@Override
					public void focusLost(FocusEvent e) {

						infoLabel.setBorder(BorderFactory.createEmptyBorder());
					}

				});

				helpListener.setHelpTopic(HELP_SIGNPIN);

				GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
				mainPanel.setLayout(mainPanelLayout);

				GroupLayout.SequentialGroup infoHorizontal = mainPanelLayout
						.createSequentialGroup().addComponent(infoLabel);
				GroupLayout.ParallelGroup infoVertical = mainPanelLayout
						.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(infoLabel);

				if (!renderHeaderPanel) {

					if (helpListener.implementsListener()) {
						infoHorizontal.addPreferredGap(
								LayoutStyle.ComponentPlacement.UNRELATED, 0,
								Short.MAX_VALUE).addComponent(helpLabel);
						infoVertical.addComponent(helpLabel);
					}

				}

				mainPanelLayout.setHorizontalGroup(infoHorizontal);
				mainPanelLayout.setVerticalGroup(infoVertical);

				enterPINButton.setFont(enterPINButton.getFont().deriveFont(
						enterPINButton.getFont().getStyle()
								& ~java.awt.Font.BOLD));
				enterPINButton.setText(getMessage(BUTTON_SIGN));
				enterPINButton.setActionCommand(enterPINCommand);
				enterPINButton.addActionListener(enterPINListener);

				if (renderCancelButton) {
					cancelButton.setFont(cancelButton.getFont().deriveFont(
							cancelButton.getFont().getStyle()
									& ~java.awt.Font.BOLD));
					cancelButton.setText(getMessage(BUTTON_CANCEL));
					cancelButton.setActionCommand(cancelCommand);
					cancelButton.addActionListener(cancelListener);
				}

				updateMethodToRunAtResize("at.gv.egiz.bku.gui.BKUGUIImpl",
						"renderShowSignatureDataDialogButtonPanel");

				renderShowSignatureDataDialogButtonPanel();

        if (windowCloseAdapter != null) {
          windowCloseAdapter.registerListener(cancelListener, cancelCommand);
        }

				primaryFocusHolder = enterPINButton;

				enterPINButton.requestFocus();

				contentPanel.validate();

				resize();
			}
		});
	}

	public void renderShowSignatureDataDialogButtonPanel() {

		GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
		buttonPanel.setLayout(buttonPanelLayout);

		GroupLayout.SequentialGroup buttonHorizontal = buttonPanelLayout
				.createSequentialGroup();
		GroupLayout.Group buttonVertical;

		if (renderCancelButton) {

			buttonHorizontal.addComponent(enterPINButton,
					GroupLayout.PREFERRED_SIZE, buttonSize,
					GroupLayout.PREFERRED_SIZE).addPreferredGap(
					LayoutStyle.ComponentPlacement.RELATED).addComponent(
					cancelButton, GroupLayout.PREFERRED_SIZE, buttonSize,
					GroupLayout.PREFERRED_SIZE);
			buttonVertical = buttonPanelLayout.createParallelGroup(
					GroupLayout.Alignment.BASELINE)
					.addComponent(enterPINButton).addComponent(cancelButton);
		} else {
			buttonHorizontal.addComponent(enterPINButton,
					GroupLayout.PREFERRED_SIZE, buttonSize,
					GroupLayout.PREFERRED_SIZE);
			buttonVertical = buttonPanelLayout.createSequentialGroup()
					.addComponent(enterPINButton);
		}

		buttonPanelLayout.setHorizontalGroup(buttonHorizontal);
		buttonPanelLayout.setVerticalGroup(buttonVertical);

	}

	@Override
	public void correctionButtonPressed() {
		log.debug("[{}] Correction button pressed.", Thread.currentThread().getName());

		if (pinpadPIN != null) {
			try {
				pinpadPIN.remove(0, 1);
			} catch (BadLocationException ex) {
			}
		}
	}

	@Override
	public void allKeysCleared() {
		log.debug("[{}] All keys cleared.", Thread.currentThread().getName());

		if (pinpadPIN != null) {
			try {
				pinpadPIN.remove(0, pinpadPIN.getLength());
			} catch (BadLocationException ex) {
			}
		}
	}

	@Override
	public void validKeyPressed() {
		log.debug("[{}] Valid key pressed.", Thread.currentThread().getName());

		if (pinpadPIN != null) {
			try {
				pinpadPIN.insertString(0, "*", null);
			} catch (BadLocationException ex) {
			}
		}
	}

  @Override
  public void showSignaturePINDialog(final PinInfo pinSpec, final int numRetries,
            final ActionListener signListener, final String signCommand,
            final ActionListener cancelListener, final String cancelCommand,
            final ActionListener hashdataListener, final String hashdataCommand) {

		log.debug("Scheduling signature-pin dialog.");

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				log.debug("[{}] Show signature-pin dialog.", Thread.currentThread().getName());

				mainPanel.removeAll();
				buttonPanel.removeAll();

				// specify policy to ensure correct focus traversal
				if (useFocusTraversalPolicy) {

					contentPanel.setFocusCycleRoot(true);
					contentPanel
							.setFocusTraversalPolicy(new AdvancedSigPinGUIFocusTraversalPolicy());
				}

				if (renderHeaderPanel) {
					if (numRetries < 0) {
						titleLabel.setText(getMessage(TITLE_SIGN));
					} else {
						titleLabel.setText(getMessage(TITLE_RETRY));
					}
				}

				infoLabel = new JLabel();
				if (numRetries < 0) {
					infoLabel.setFont(infoLabel.getFont().deriveFont(
							infoLabel.getFont().getStyle()
									& ~java.awt.Font.BOLD));
					if (shortText) {
						infoLabel
								.setText(getMessage(MESSAGE_HASHDATALINK_TINY));
					} else {
						infoLabel.setText(getMessage(MESSAGE_HASHDATALINK));
					}

					infoLabel.setToolTipText(getMessage(SIGDATA_TOOLTIPTEXT));
					infoLabel.getAccessibleContext().setAccessibleDescription(
							getMessage(SIGDATA_TOOLTIPTEXT));
					infoLabel.getAccessibleContext().setAccessibleName(
							getMessage(SIGDATA_TOOLTIPTEXT));

					infoLabel.setFocusable(true);
					infoLabel.setCursor(Cursor
							.getPredefinedCursor(Cursor.HAND_CURSOR));
					infoLabel.setForeground(HYPERLINK_COLOR);
					infoLabel.addMouseListener(new MouseAdapter() {

						@Override
						public void mouseClicked(MouseEvent me) {
							ActionEvent e = new ActionEvent(this,
									ActionEvent.ACTION_PERFORMED,
									hashdataCommand);
							hashdataListener.actionPerformed(e);
						}
					});

					infoLabel.addKeyListener(new KeyAdapter() {

						@Override
						public void keyPressed(KeyEvent e) {

							if (e.getKeyCode() == KeyEvent.VK_ENTER) {
								ActionEvent e1 = new ActionEvent(this,
										ActionEvent.ACTION_PERFORMED,
										hashdataCommand);
								hashdataListener.actionPerformed(e1);
							}
						}

					});

					infoLabel.addFocusListener(new FocusAdapter() {

						@Override
						public void focusGained(FocusEvent e) {

							infoLabel.setBorder(sigDataFocusBorder);
						}

						@Override
						public void focusLost(FocusEvent e) {

							infoLabel.setBorder(BorderFactory
									.createEmptyBorder());
						}

					});

					helpListener.setHelpTopic(HELP_SIGNPIN);
				} else {
					String retryPattern;
					if (numRetries < 2) {
						retryPattern = getMessage(MESSAGE_LAST_RETRY);
					} else {
						retryPattern = getMessage(MESSAGE_RETRIES);
					}
					infoLabel.setFocusable(true);
					infoLabel.setText(MessageFormat.format(retryPattern,
							new Object[] { String.valueOf(numRetries) }));

					infoLabel.setToolTipText(getMessage(SIGDATA_TOOLTIPTEXT));
					infoLabel.getAccessibleContext().setAccessibleDescription(
							getMessage(SIGDATA_TOOLTIPTEXT));
					infoLabel.getAccessibleContext().setAccessibleName(
							getMessage(SIGDATA_TOOLTIPTEXT));

					infoLabel.setFont(infoLabel.getFont()
							.deriveFont(
									infoLabel.getFont().getStyle()
											| java.awt.Font.BOLD));
					infoLabel.setForeground(ERROR_COLOR);
					helpListener.setHelpTopic(HELP_RETRY);
				}

				signButton.setFont(signButton.getFont().deriveFont(
						signButton.getFont().getStyle() & ~java.awt.Font.BOLD));
				signButton.setText(getMessage(BUTTON_SIGN));
				signButton.setEnabled(pinSpec.getMinLength() <= 0);
				signButton.setActionCommand(signCommand);
				signButton.addActionListener(signListener);

				signPinLabel.setFont(signPinLabel.getFont()
						.deriveFont(
								signPinLabel.getFont().getStyle()
										& ~java.awt.Font.BOLD));
				String pinLabel = getMessage(LABEL_PIN);
				signPinLabel.setText(MessageFormat.format(pinLabel,
						new Object[] { pinSpec.getLocalizedName() }));

				pinField = new JPasswordField();
				pinField.setText("");
				pinField.setName("PINField");
				pinField.setDocument(new PINDocument(pinSpec.getMinLength(),
						pinSpec.getMaxLength(), pinSpec.getRegexpPattern(),
						signButton));
				pinField.setActionCommand(signCommand);
				pinField.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (pinField.getPassword().length >= pinSpec
								.getMinLength()) {
							signListener.actionPerformed(e);
						}
					}
				});

				pinsizeLabel.setFont(pinsizeLabel.getFont()
						.deriveFont(
								pinsizeLabel.getFont().getStyle()
										& ~java.awt.Font.BOLD,
								pinsizeLabel.getFont().getSize() - 2));
				pinsizeLabel.setText(MessageFormat
						.format(getMessage(LABEL_PINSIZE), pinSpec
								.getLocalizedLength()));

				pinField.getAccessibleContext().setAccessibleDescription(
						infoLabel.getText() + signPinLabel.getText()
								+ pinsizeLabel.getText());

				GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
				mainPanel.setLayout(mainPanelLayout);

				GroupLayout.SequentialGroup infoHorizontal = mainPanelLayout
						.createSequentialGroup().addComponent(infoLabel);
				GroupLayout.ParallelGroup infoVertical = mainPanelLayout
						.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(infoLabel);

				if (!renderHeaderPanel) {

					if (helpListener.implementsListener()) {
						infoHorizontal.addPreferredGap(
								LayoutStyle.ComponentPlacement.UNRELATED, 0,
								Short.MAX_VALUE).addComponent(helpLabel);
						infoVertical.addComponent(helpLabel);
					}

				}

				// align pinfield and pinsize to the right
				GroupLayout.Group pinHorizontal = mainPanelLayout
						.createParallelGroup(GroupLayout.Alignment.TRAILING);
				GroupLayout.SequentialGroup pinVertical = mainPanelLayout
						.createSequentialGroup();

				if (pinLabelPos == PinLabelPosition.ABOVE) {
					pinHorizontal.addGroup(
							mainPanelLayout.createParallelGroup(
									GroupLayout.Alignment.LEADING)
									.addComponent(signPinLabel,
											GroupLayout.PREFERRED_SIZE,
											GroupLayout.DEFAULT_SIZE,
											GroupLayout.PREFERRED_SIZE)
									.addComponent(pinField,
											GroupLayout.PREFERRED_SIZE,
											GroupLayout.DEFAULT_SIZE,
											Short.MAX_VALUE)).addComponent(
							pinsizeLabel, GroupLayout.PREFERRED_SIZE,
							GroupLayout.DEFAULT_SIZE,
							GroupLayout.PREFERRED_SIZE);
					pinVertical.addComponent(signPinLabel).addPreferredGap(
							LayoutStyle.ComponentPlacement.RELATED)
							.addComponent(pinField, GroupLayout.PREFERRED_SIZE,
									GroupLayout.DEFAULT_SIZE,
									GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(
									LayoutStyle.ComponentPlacement.RELATED)
							.addComponent(pinsizeLabel);
				} else { // PinLabelPosition.LEFT
					pinHorizontal
							.addGroup(
									mainPanelLayout
											.createSequentialGroup()
											.addComponent(signPinLabel,
													GroupLayout.PREFERRED_SIZE,
													GroupLayout.DEFAULT_SIZE,
													GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(
													LayoutStyle.ComponentPlacement.RELATED)
											.addComponent(pinField,
													GroupLayout.PREFERRED_SIZE,
													GroupLayout.DEFAULT_SIZE,
													Short.MAX_VALUE))
							.addComponent(pinsizeLabel,
									GroupLayout.PREFERRED_SIZE,
									GroupLayout.DEFAULT_SIZE,
									GroupLayout.PREFERRED_SIZE);
					pinVertical.addGroup(
							mainPanelLayout.createParallelGroup(
									GroupLayout.Alignment.BASELINE)
									.addComponent(signPinLabel).addComponent(
											pinField)).addPreferredGap(
							LayoutStyle.ComponentPlacement.RELATED)
							.addComponent(pinsizeLabel);
				}

				mainPanelLayout.setHorizontalGroup(mainPanelLayout
						.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(infoHorizontal).addGroup(pinHorizontal));

				mainPanelLayout
						.setVerticalGroup(mainPanelLayout
								.createSequentialGroup().addGroup(infoVertical)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(pinVertical));

				if (renderCancelButton) {
					cancelButton.setFont(cancelButton.getFont().deriveFont(
							cancelButton.getFont().getStyle()
									& ~java.awt.Font.BOLD));
					cancelButton.setText(getMessage(BUTTON_CANCEL));
					cancelButton.setActionCommand(cancelCommand);
					cancelButton.addActionListener(cancelListener);
				}

				updateMethodToRunAtResize("at.gv.egiz.bku.gui.BKUGUIImpl",
						"renderSignaturePINDialogueButtonPanel");

				renderSignaturePINDialogueButtonPanel();

        if (windowCloseAdapter != null) {
          windowCloseAdapter.registerListener(cancelListener, cancelCommand);
        }

				primaryFocusHolder = pinField;

				pinField.requestFocus();

				contentPanel.validate();

				resize();
			}
		});
	}

	public void renderSignaturePINDialogueButtonPanel() {

		GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
		buttonPanel.setLayout(buttonPanelLayout);

		GroupLayout.SequentialGroup buttonHorizontal = buttonPanelLayout
				.createSequentialGroup();
		GroupLayout.Group buttonVertical;

		if (renderCancelButton) {

			buttonHorizontal.addComponent(signButton,
					GroupLayout.PREFERRED_SIZE, buttonSize,
					GroupLayout.PREFERRED_SIZE).addPreferredGap(
					LayoutStyle.ComponentPlacement.RELATED).addComponent(
					cancelButton, GroupLayout.PREFERRED_SIZE, buttonSize,
					GroupLayout.PREFERRED_SIZE);
			buttonVertical = buttonPanelLayout.createParallelGroup(
					GroupLayout.Alignment.BASELINE).addComponent(signButton)
					.addComponent(cancelButton);
		} else {
			buttonHorizontal.addComponent(signButton,
					GroupLayout.PREFERRED_SIZE, buttonSize,
					GroupLayout.PREFERRED_SIZE);
			buttonVertical = buttonPanelLayout.createSequentialGroup()
					.addComponent(signButton);
		}

		buttonPanelLayout.setHorizontalGroup(buttonHorizontal);
		buttonPanelLayout.setVerticalGroup(buttonVertical);

	}

	@Override
	public void showErrorDialog(final String errorMsgKey,
			final Object[] errorMsgParams, final ActionListener okListener,
			final String okCommand) {

		showMessageDialog(TITLE_ERROR, ERROR_COLOR, errorMsgKey,
				errorMsgParams, BUTTON_OK, okListener, okCommand);
	}

	@Override
	public void showErrorDialog(final String errorMsgKey,
			final Object[] errorMsgParams) {

		showMessageDialog(TITLE_ERROR, ERROR_COLOR, errorMsgKey,
				errorMsgParams, null, null, null);
	}

	@Override
	public void showWarningDialog(final String warningMsgKey,
			final Object[] warningMsgParams, final ActionListener okListener,
			final String okCommand) {

		showMessageDialog(TITLE_WARNING, WARNING_COLOR, warningMsgKey,
				warningMsgParams, BUTTON_OK, okListener, okCommand);
	}

	@Override
	public void showWarningDialog(final String warningMsgKey,
			final Object[] warningMsgParams) {

		showMessageDialog(TITLE_WARNING, WARNING_COLOR, warningMsgKey,
				warningMsgParams, null, null, null);
	}

	@Override
	public void showMessageDialog(final String titleKey, final String msgKey,
			final Object[] msgParams, final String buttonKey,
			final ActionListener okListener, final String okCommand) {

		showMessageDialog(titleKey, null, msgKey, msgParams, buttonKey,
				okListener, okCommand);
	}

	@Override
	public void showMessageDialog(final String titleKey, final String msgKey,
			final Object[] msgParams) {

		showMessageDialog(titleKey, null, msgKey, msgParams, null, null, null);
	}

	@Override
	public void showMessageDialog(final String titleKey, final String msgKey) {

		showMessageDialog(titleKey, null, msgKey, null, null, null, null);
	}

	/**
	 * 
	 * @param buttonKey
	 *            if null defaults to BUTTON_OK
	 */
	private void showMessageDialog(final String titleKey,
			final Color titleColor, final String msgKey,
			final Object[] msgParams, final String buttonKey,
			final ActionListener okListener, final String okCommand) {

		log.debug("Scheduling message dialog.");

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				log.debug("[{}] Show message dialog.", Thread.currentThread().getName());

				log.debug("ButtonKey: {}.", buttonKey);

				mainPanel.removeAll();
				buttonPanel.removeAll();

				if (renderHeaderPanel) {
					titleLabel.setText(getMessage(titleKey));
				}

				helpListener.setHelpTopic(msgKey);

				String msgPattern = getMessage(msgKey);
				String msg = MessageFormat.format(msgPattern, msgParams);

				// we need to create a new JLabel object every time in order to
				// ensure
				// that screen reading software will read each updated label
				msgLabel = new JLabel();

				msgLabel.setFocusable(true);

				msgLabel.setFont(msgLabel.getFont().deriveFont(
						msgLabel.getFont().getStyle() & ~Font.BOLD));
				msgLabel.setText(msg);

				GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
				mainPanel.setLayout(mainPanelLayout);

				GroupLayout.ParallelGroup mainHorizontal = mainPanelLayout
						.createParallelGroup(GroupLayout.Alignment.LEADING);
				GroupLayout.SequentialGroup mainVertical = mainPanelLayout
						.createSequentialGroup();

				String accessibleData = "";

				if (!renderHeaderPanel) {
					msgTitleLabel = new JLabel();
					msgTitleLabel.setFont(msgTitleLabel.getFont().deriveFont(
							msgTitleLabel.getFont().getStyle() | Font.BOLD));
					msgTitleLabel.setText(getMessage(titleKey));

					if (titleColor != null) {
						msgTitleLabel.setForeground(titleColor);
					}

					accessibleData = accessibleData + getMessage(titleKey);

					GroupLayout.SequentialGroup titleHorizontal = mainPanelLayout
							.createSequentialGroup()
							.addComponent(msgTitleLabel);

					GroupLayout.ParallelGroup titleVertical = mainPanelLayout
							.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(msgTitleLabel);

					if (helpListener.implementsListener()) {
						titleHorizontal.addPreferredGap(
								LayoutStyle.ComponentPlacement.UNRELATED, 0,
								Short.MAX_VALUE).addComponent(helpLabel);
						titleVertical.addComponent(helpLabel);
					}

					mainHorizontal.addGroup(titleHorizontal);
					mainVertical.addGroup(titleVertical);

				} else {

					accessibleData = accessibleData + titleLabel.getText();
				}

				msgLabel.getAccessibleContext().setAccessibleName(
						accessibleData + msgLabel.getText());
				msgLabel.getAccessibleContext().setAccessibleDescription(
						accessibleData + msgLabel.getText());

				mainPanelLayout.setHorizontalGroup(mainHorizontal
						.addComponent(msgLabel));
				mainPanelLayout.setVerticalGroup(mainVertical
						.addComponent(msgLabel));

				if (okListener != null) {

					showMessageOKButton = true;

					okButton.setFont(okButton.getFont()
							.deriveFont(
									okButton.getFont().getStyle()
											& ~java.awt.Font.BOLD));
					okButton.setText(getMessage((buttonKey != null) ? buttonKey
							: BUTTON_OK));
					okButton.setActionCommand(okCommand);
					okButton.addActionListener(okListener);

					renderShowMessageDialogueButtonPanel();

					primaryFocusHolder = okButton;

				} else {
					log.debug("No okListener configured.");
					showMessageOKButton = false;
				}

				// okListener might be null (up to windowCloseAdapter what to do)
				if (windowCloseAdapter != null) {
					windowCloseAdapter.registerListener(okListener, okCommand);
				}

				updateMethodToRunAtResize("at.gv.egiz.bku.gui.BKUGUIImpl",
						"renderShowMessageDialogueButtonPanel");

				// put focus to msgLabel to guarantee that label is read by
				// screen reader upon loading
				msgLabel.requestFocus();
				msgLabel.setFocusable(false);

				contentPanel.validate();

				resize();
			}
		});
	}

	public void renderShowMessageDialogueButtonPanel() {

		if (showMessageOKButton) {
			GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
			buttonPanel.setLayout(buttonPanelLayout);

			buttonPanelLayout.setHorizontalGroup(buttonPanelLayout
					.createSequentialGroup().addComponent(okButton,
							GroupLayout.PREFERRED_SIZE, buttonSize,
							GroupLayout.PREFERRED_SIZE));
			buttonPanelLayout.setVerticalGroup(buttonPanelLayout
					.createSequentialGroup().addComponent(okButton));
		}
	}

	@Override
	public char[] getPin() {
		if (pinField != null) {
			char[] pin = pinField.getPassword(); // returns a copy
			pinField = null; // garbage collect original pin (make sure to clear
			// char[] after use)
			return pin;
		}
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////
	// SECURE VIEWER
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * @param signedReferences
	 * @param backListener
	 *            gets notified if pin-dialog has to be redrawn
	 *            (signedRefencesList returns via BACK button)
	 * @param okCommand
	 */
	@Override
	public void showSecureViewer(final List<HashDataInput> dataToBeSigned,
			final ActionListener backListener, final String backCommand) {

		if (dataToBeSigned == null) {
			showErrorDialog(getMessage(ERR_NO_HASHDATA),
					new Object[] { "no signature data provided" },
					backListener, backCommand);
		} else if (dataToBeSigned.size() == 1) {
			// TODO pull out (see also SignedReferencesSelectionListener)
			if (SecureViewerDialog.SUPPORTED_MIME_TYPES.contains(dataToBeSigned
					.get(0).getMimeType())) {
				try {
					log.debug("[{}] Scheduling secure viewer.", Thread.currentThread().getName());

					showMessageDialog(TITLE_SIGNATURE_DATA,
							MESSAGE_HASHDATA_VIEWER);

					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							try {
								showSecureViewer(dataToBeSigned.get(0),
										backListener, backCommand);
							} catch (FontProviderException ex) {
								log.error("Failed to display secure viewer.", ex);
								showErrorDialog(ERR_VIEWER, new Object[] { ex
										.getMessage() }, backListener,
										backCommand);
							}
						}
					});

				} catch (Exception ex) { // InterruptedException
					// InvocationTargetException
					log.error("Failed to display secure viewer. ", ex);
					showErrorDialog(ERR_UNKNOWN, null, backListener,
							backCommand);
				}
			} else {
				log.debug("[{}] mime-type not supported by secure viewer, " +
						"scheduling save dialog.", Thread.currentThread().getName());
				showMessageDialog(TITLE_SIGNATURE_DATA,
						MESSAGE_UNSUPPORTED_MIMETYPE,
						new Object[] { dataToBeSigned.get(0).getMimeType() });
				SecureViewerSaveDialog.showSaveDialog(contentPane, dataToBeSigned.get(0),
						messages, backListener, backCommand,
						(int) (baseFontSize * getResizeFactor()));
			}
		} else {
			showSignedReferencesListDialog(dataToBeSigned, backListener,
					backCommand);
		}
	}

	/**
	 * has to be called from event dispatcher thread
	 */
	private void showSecureViewer(HashDataInput dataToBeSigned,
			ActionListener closeListener, String closeCommand)
			throws FontProviderException {

		log.debug("[{}] Show secure viewer.", Thread.currentThread().getName());
		secureViewer = new SecureViewerDialog(null, messages, closeListener,
				closeCommand, fontProvider, helpListener, getResizeFactor());

		// workaround for [#439]
		// avoid AlwaysOnTop at least in applet, otherwise make secureViewer
		// AlwaysOnTop since MOCCA Dialog (JFrame created in LocalSTALFactory)
		// is always on top.
		//Window window = SwingUtilities.getWindowAncestor(contentPane);
		//if (window != null && window.isAlwaysOnTop()) {
			log.debug("Make secureViewer alwaysOnTop.");
			secureViewer.setAlwaysOnTop(true);
		//}

		secureViewer.setContent(dataToBeSigned);
		log.trace("Viewer setContent returned.");
	}

	private void openSecureViewerDialog() {
		
		final HashDataInput storedSelection = signedReferences.get(referenceIndex);
		
		if (SecureViewerDialog.SUPPORTED_MIME_TYPES.contains(storedSelection
				.getMimeType())) {
			log.debug("[{}] Scheduling secure viewer dialog.", Thread.currentThread().getName());

			showMessageDialog(TITLE_SIGNATURE_DATA,
					MESSAGE_HASHDATA_VIEWER);

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					try {
						showSecureViewer(storedSelection, storedBackToListListener,
								null);
						// SecureViewerDialog.showSecureViewer(selection,
						// messages, fontProvider,
						// helpMouseListener.getActionListener(),
						// false);
					} catch (FontProviderException ex) {
						log.error("Failed to display secure viewer.", ex);
						showErrorDialog(BKUGUIFacade.ERR_VIEWER,
								new Object[] { ex.getMessage() },
								storedBackToListListener, null);
					}

				}
			});
		} else {
			log.debug("[{}] Mime-type not supported by secure viewer, " +
					"scheduling save dialog.", Thread.currentThread().getName());
			showMessageDialog(BKUGUIFacade.TITLE_SIGNATURE_DATA,
					BKUGUIFacade.MESSAGE_UNSUPPORTED_MIMETYPE,
					new Object[] { storedSelection.getMimeType() });
			SecureViewerSaveDialog.showSaveDialog(contentPane, storedSelection, messages,
					storedBackToListListener, null,
					(int) (baseFontSize * getResizeFactor()));
		}		
		
		
	}
	
	private void showSignedReferencesListDialog(
			final List<HashDataInput> signedReferences,
			final ActionListener backListener, final String backCommand) {

		log.debug("[{}] Scheduling signed references list dialog.", Thread.currentThread().getName());

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				log.debug("[{}] Show signed references list dialog.", Thread.currentThread().getName());

				mainPanel.removeAll();
				buttonPanel.removeAll();

				if (renderHeaderPanel) {
					titleLabel.setText(getMessage(TITLE_SIGNATURE_DATA));
				}

				helpListener.setHelpTopic(HELP_HASHDATALIST);

				refIdLabel = new JLabel();
				refIdLabel.setFont(refIdLabel.getFont().deriveFont(
						refIdLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
				String refIdLabelPattern = getMessage(MESSAGE_HASHDATALIST);
				refIdLabel.setText(MessageFormat.format(refIdLabelPattern,
						new Object[] { signedReferences.size() }));

				HashDataTableModel tableModel = new HashDataTableModel(
						signedReferences, renderRefId);
				hashDataTable = new JTable(tableModel);
				
				hyperlinkRenderer = new HyperlinkRenderer(renderRefId);
				
				hashDataTable.setDefaultRenderer(HashDataInput.class,
						hyperlinkRenderer);
				hashDataTable.setTableHeader(null);

				hashDataTable
						.addMouseMotionListener(new SignedReferencesMouseMotionListener(
								hashDataTable));
				
				hashDataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

				hashDataTable
						.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				hashDataTable.getSelectionModel().addListSelectionListener(
						new SignedReferencesSelectionListener(signedReferences,
								backListener, backCommand));
				
				
				hashDataTable.addMouseListener(new MouseAdapter() {
					
					@Override
					public void mouseClicked(MouseEvent e) {
						
						openSecureViewerDialog();
					}

				});

				hashDataTable.addKeyListener(new KeyAdapter() {
					
					@Override
					public void keyPressed(KeyEvent e) {
						
						if(e.getKeyCode() == KeyEvent.VK_ENTER) {
							
							log.debug("Detected Enter Key.");
							
							openSecureViewerDialog();
						}
						
					}
					
				});

				hashDataScrollPane = new JScrollPane(hashDataTable);

				backButton.setFont(backButton.getFont().deriveFont(
						backButton.getFont().getStyle() & ~java.awt.Font.BOLD));
				backButton.setText(getMessage(BUTTON_BACK));
				backButton.setActionCommand(backCommand);
				backButton.addActionListener(backListener);				

				primaryFocusHolder = hashDataTable;
				
				updateMethodToRunAtResize("at.gv.egiz.bku.gui.BKUGUIImpl", "renderSignedReferenceListButtonandTable");
				
				renderSignedReferenceListButtonandTable();

				hashDataTable.requestFocus();
				
				contentPanel.validate();
				
				resize();
			}
		});
	}

	public void renderSignedReferenceListButtonandTable() {
		
		GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
		mainPanel.setLayout(mainPanelLayout);

		GroupLayout.SequentialGroup messageHorizontal = mainPanelLayout
				.createSequentialGroup().addComponent(refIdLabel);

		GroupLayout.ParallelGroup messageVertical = mainPanelLayout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(refIdLabel);

		if (!renderHeaderPanel) {

			if (helpListener.implementsListener()) {
				messageHorizontal.addPreferredGap(
						LayoutStyle.ComponentPlacement.UNRELATED, 0,
						Short.MAX_VALUE).addComponent(helpLabel);
				messageVertical.addComponent(helpLabel);
			}
		}

		mainPanelLayout.setHorizontalGroup(mainPanelLayout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(messageHorizontal).addComponent(
						hashDataScrollPane, 0, 0, Short.MAX_VALUE));

		mainPanelLayout
				.setVerticalGroup(mainPanelLayout
						.createSequentialGroup()
						.addGroup(messageVertical)
						.addPreferredGap(
								LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(
								hashDataScrollPane,
								0,
								0,
								hashDataTable.getPreferredSize().height + 3));



		GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
		buttonPanel.setLayout(buttonPanelLayout);

		buttonPanelLayout.setHorizontalGroup(buttonPanelLayout
				.createSequentialGroup().addComponent(backButton,
						GroupLayout.PREFERRED_SIZE, buttonSize,
						GroupLayout.PREFERRED_SIZE));
		buttonPanelLayout.setVerticalGroup(buttonPanelLayout
				.createSequentialGroup().addComponent(backButton));
		
	}
	
	/**
	 * not possible to add mouse listener to TableCellRenderer to change cursor
	 * on specific columns only, use table.columnAtPoint(e.getPoint())
	 * 
	 */
	private class SignedReferencesMouseMotionListener extends
			MouseMotionAdapter {

		JTable hashDataTable;

		public SignedReferencesMouseMotionListener(JTable table) {
			this.hashDataTable = table;
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			// if (hashDataTable.columnAtPoint(e.getPoint()) == 0) {
			hashDataTable.setCursor(Cursor
					.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
	}

	// /////////
	// SignedReferencesList (TODO pull out)

	public class SignedReferencesSelectionListener implements
			ListSelectionListener {

//		List<HashDataInput> signedReferences;
		ActionListener backListener;
		String backCommand;

		public SignedReferencesSelectionListener(
				List<HashDataInput> signedReferences,
				ActionListener backListener, String backCommand) {
//			this.signedReferences = signedReferences;
			BKUGUIImpl.this.signedReferences = signedReferences;
			this.backListener = backListener;
			this.backCommand = backCommand;
		}

		@Override
		public void valueChanged(ListSelectionEvent event) {

			if (event.getValueIsAdjusting()) {
				return;
			}

			ListSelectionModel lsm = (ListSelectionModel) event.getSource();
			int selectionIdx = lsm.getMinSelectionIndex();

            log.debug("[{}] Reference {} selected.",
                Thread.currentThread().getName(), selectionIdx);

			if (selectionIdx >= 0) {
//				final HashDataInput selection = signedReferences
//						.get(selectionIdx);
//				final SignedReferencesListDisplayer backToListListener = new SignedReferencesListDisplayer(
//						signedReferences, backListener, backCommand);

				referenceIndex = selectionIdx;
				storedBackToListListener = new SignedReferencesListDisplayer(
						signedReferences, backListener, backCommand);
				
//				if (SecureViewerDialog.SUPPORTED_MIME_TYPES.contains(selection
//						.getMimeType())) {
//					log.debug("[" + Thread.currentThread().getName()
//							+ "] scheduling secure viewer dialog");
//
//					showMessageDialog(TITLE_SIGNATURE_DATA,
//							MESSAGE_HASHDATA_VIEWER);
//
//					SwingUtilities.invokeLater(new Runnable() {
//
//						@Override
//						public void run() {
//							try {
//								showSecureViewer(selection, backToListListener,
//										null);
//								// SecureViewerDialog.showSecureViewer(selection,
//								// messages, fontProvider,
//								// helpMouseListener.getActionListener(),
//								// false);
//							} catch (FontProviderException ex) {
//								log
//										.error(
//												"failed to display secure viewer",
//												ex);
//								showErrorDialog(BKUGUIFacade.ERR_VIEWER,
//										new Object[] { ex.getMessage() },
//										backToListListener, null);
//							}
//
//						}
//					});
//				} else {
//					log
//							.debug("["
//									+ Thread.currentThread().getName()
//									+ "] mime-type not supported by secure viewer, scheduling save dialog");
//					showMessageDialog(BKUGUIFacade.TITLE_SIGNATURE_DATA,
//							BKUGUIFacade.MESSAGE_UNSUPPORTED_MIMETYPE,
//							new Object[] { selection.getMimeType() });
//					SecureViewerSaveDialog.showSaveDialog(selection, messages,
//							backToListListener, null,
//							(int) (baseFontSize * getResizeFactor()));
//				}
			}
		}

		/**
		 * ActionListener that returns to signed references list
		 */
		private class SignedReferencesListDisplayer implements ActionListener {
			List<HashDataInput> sr;
			ActionListener bl;
			String bc;

			public SignedReferencesListDisplayer(
					List<HashDataInput> signedReferences,
					ActionListener backListener, String backCommand) {
				sr = signedReferences;
				bl = backListener;
				bc = backCommand;
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				// log.debug("[" + Thread.currentThread().getName() +
				// "] displaying signed references list");
				showSignedReferencesListDialog(sr, bl, bc);
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// INITIALIZERS (MAY BE OVERRIDDEN BY SUBCLASSES)
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * Load applet messages bundle. Note that getBundle looks for classes based
	 * on the default Locale before it selects the base class!
	 * 
	 * Called from constructor. Subclasses may override this method to ensure
	 * the message bundle is loaded once initButtonSize (called from constructor
	 * as well) is called. (Only relevant if initButtonSize is overridden as
	 * well)
	 * 
	 * @param locale
	 */
	protected void loadMessageBundle(Locale locale) {
    if (locale != null) {
      // see [#378] Ignoring post parameter 'locale': bundle resolve-order
			// not correct?!
			log.debug("Loading message bundle for language: {}.", locale);
			messages = ResourceBundle.getBundle(MESSAGES_BUNDLE, locale);
    } else {
      messages = ResourceBundle.getBundle(MESSAGES_BUNDLE);
    }

    if ("".equals(messages.getLocale().getLanguage())) {
      log.debug("Using locale 'default'.");
    } else {
      log.debug("Using locale '{}'.", messages.getLocale());
    }
	}

	protected int initButtonSize() {
		int bs = 0;

		JButton b = new JButton();
		b.setText(getMessage(BUTTON_OK));
		if (b.getPreferredSize().width > bs) {
			bs = b.getPreferredSize().width;
		}
		// need cancel button for message dialog,
		// even if renderCancelButton == false
		b.setText(getMessage(BUTTON_CANCEL));
		if (b.getPreferredSize().width > bs) {
			bs = b.getPreferredSize().width;
		}
		b.setText(getMessage(BUTTON_SIGN));
		if (b.getPreferredSize().width > bs) {
			bs = b.getPreferredSize().width;
		}
		b.setText(getMessage(BUTTON_BACK));
		if (b.getPreferredSize().width > bs) {
			bs = b.getPreferredSize().width;
		}
		b.setText(getMessage(BUTTON_SAVE));
		if (b.getPreferredSize().width > bs) {
			bs = b.getPreferredSize().width;
		}
		return bs;
	}

	@Override
	public void getFocusFromBrowser() {

		log.debug("Try setting focus to current component ...");
		if (primaryFocusHolder == null) {
			log.debug("No stored component - set focus to contentPanel ...");

			primaryFocusHolder = contentPanel;

		}
		log.debug("Component to obtain focus: {}.", primaryFocusHolder.getName());
		primaryFocusHolder.requestFocus();

	}

	protected void updateHelpLabelIcon() {

		if (helpListener.implementsListener()) {

			helpLabel.setIcon(new ImageIcon(getClass().getResource(
					getHelpLabelResourceName())));

			helpLabel.setBorder(helpLabel.hasFocus() ? helpFocusBorder
					: BorderFactory.createEmptyBorder());
		}

	}

	protected String getHelpLabelResourceName() {

		double contentPanelWidth = contentPanel.getSize().getWidth();
		String resourceName = HELP_IMG;

		if (contentPanelWidth > 300) {

			resourceName = HELP_IMG_L;
		}

		if (contentPanelWidth > 470) {

			resourceName = HELP_IMG_XL;
		}

		if (contentPanelWidth > 600) {

			resourceName = HELP_IMG_XXL;
		}

		return resourceName;
	}

	protected float getResizeFactor(int maxBaseWidth) {
		if (baseWidth == null || baseHeight == null || baseWidth == 0
				|| baseHeight == 0) {

			// first call - determine base width and height
			int width = contentPanel.getWidth();
			int height = contentPanel.getHeight();
			float ratio = ((float) width / height);
			baseWidth = width < maxBaseWidth ? width : maxBaseWidth;
			baseHeight = (int) (baseWidth / ratio);
			if (baseHeight > height) {
				baseHeight = height;
				baseWidth = (int) (baseHeight * ratio);
			}
			log.debug("Original gui size: " + width + "x" + height +
					" - Base: " + baseWidth + "x" + baseHeight);
		}

		float factor = (float) contentPanel.getSize().getWidth() / baseWidth;
		return factor;
	}

	protected float getResizeFactor() {
		if (baseWidth == null || baseHeight == null || baseWidth == 0
				|| baseHeight == 0) {

			// first call - determine base width and height
			baseWidth = contentPanel.getWidth();
			baseHeight = contentPanel.getHeight();
		}

		float factor = (float) contentPanel.getSize().getWidth() / baseWidth;
		return factor;
	}

	public void resize() {

		log.debug("Resizing ...");
		updateHelpLabelIcon();

		float factor = getResizeFactor(166);

		this.sigDataFocusBorder.setBorderWidthFactor(factor);
		this.helpFocusBorder.setBorderWidthFactor(factor);

		buttonSize = (int) ((float) baseButtonSize * factor);

		if (renderHeaderPanel) {

			titleLabel.setFont(titleLabel.getFont().deriveFont(
					(float) ((baseFontSize + 2) * factor)));
		}

		if (cancelButton != null) {

			cancelButton.setFont(cancelButton.getFont().deriveFont(
					(float) (baseFontSize * factor)));
		}

		if (pinField != null) {
			pinField.setFont(pinField.getFont().deriveFont(
					(float) (baseFontSize * factor)));

		}

		if (infoLabel != null) {
			infoLabel.setFont(infoLabel.getFont().deriveFont(
					(float) (baseFontSize * factor)));
		}

		if (pinsizeLabel != null) {
			pinsizeLabel.setFont(pinsizeLabel.getFont().deriveFont(
					(float) ((baseFontSize * factor) - 2)));

		}

		if (signPinLabel != null) {

			signPinLabel.setFont(signPinLabel.getFont().deriveFont(
					(float) (baseFontSize * factor)));

		}

		if (signButton != null) {

			signButton.setFont(signButton.getFont().deriveFont(
					(float) (baseFontSize * factor)));

		}

		if (cardPinLabel != null) {

			cardPinLabel.setFont(cardPinLabel.getFont().deriveFont(
					(float) (baseFontSize * factor)));

		}

		if (okButton != null) {

			okButton.setFont(okButton.getFont().deriveFont(
					(float) (baseFontSize * factor)));

		}

		if (pinLabel != null) {

			pinLabel.setFont(pinLabel.getFont().deriveFont(
					(float) (baseFontSize * factor)));

		}

		if (pinpadPINField != null) {

			pinpadPINField.setFont(pinpadPINField.getFont().deriveFont(
					(float) (baseFontSize * factor)));

		}

		if (msgTitleLabel != null) {

			msgTitleLabel.setFont(msgTitleLabel.getFont().deriveFont(
					(float) (baseFontSize * factor)));

		}

		if (msgLabel != null) {

			msgLabel.setFont(msgLabel.getFont().deriveFont(
					(float) (baseFontSize * factor)));

		}

		if (enterPINButton != null) {

			enterPINButton.setFont(enterPINButton.getFont().deriveFont(
					(float) (baseFontSize * factor)));

		}
		
		if (refIdLabel != null) {

			refIdLabel.setFont(refIdLabel.getFont().deriveFont(
					(float) (baseFontSize * factor)));
		}


		if (backButton != null) {

			backButton.setFont(backButton.getFont().deriveFont(
					(float) (baseFontSize * factor)));

		}
		
		if (hyperlinkRenderer != null) {

			hyperlinkRenderer.setFontSize((int) (baseFontSize * factor));
		}

		if (hashDataTable != null) {

			hashDataTable.setRowHeight((int) (baseTableRowHeight * factor));

		}
		
		if (secureViewer != null && secureViewer.isVisible()) {

			secureViewer.resize(factor);
		}

		try {

			if (methodToRunAtResize != null) {
				log.debug("Running required button panel renderer ...");
				methodToRunAtResize.invoke(this);
			} else {
				log.debug("No MethodToRun stored.");
			}

		} catch (IllegalArgumentException e) {
			log.error("Cannot invoke rendering method.", e);
		} catch (IllegalAccessException e) {
			log.error("Cannot invoke rendering method.", e);
		} catch (InvocationTargetException e) {
			log.error("Cannot invoke rendering method.", e);
		}

		contentPanel.validate();

		log.debug("Resize done.");

	}

	// TODO: Define FocusTraversalPolicies for other GUIs as well, even if
	// focus order is currently correct by chance for other GUIs
	public class AdvancedSigPinGUIFocusTraversalPolicy extends
			FocusTraversalPolicy {

		@Override
		public Component getComponentAfter(Container container,
				Component component) {

			if (component.equals(pinField)) {

				if (signButton.isEnabled()) {

					return signButton;
				} else {

					return cancelButton;
				}
			}

			if (component.equals(signButton)) {

				return cancelButton;
			}

			if (component.equals(cancelButton)) {

				return infoLabel;
			}
			if (component.equals(infoLabel)) {

				if (helpLabel != null && helpLabel.isVisible()) {

					return helpLabel;
				} else {

					return null;
				}
			}

			if (component.equals(helpLabel)) {

				return null;
			}

			// default
			return pinField;
		}

		@Override
		public Component getComponentBefore(Container container,
				Component component) {

			if (component.equals(pinField)) {

				return null;
			}

			if (component.equals(signButton)) {

				return pinField;
			}

			if (component.equals(cancelButton)) {

				if (signButton != null && signButton.isVisible()
						&& signButton.isEnabled()) {

					return signButton;

				} else {

					return pinField;
				}
			}

			if (component.equals(infoLabel)) {

				return cancelButton;
			}

			if (component.equals(helpLabel)) {

				return infoLabel;
			}

			// default
			return pinField;
		}

		@Override
		public Component getDefaultComponent(Container container) {

			return pinField;
		}

		@Override
		public Component getFirstComponent(Container container) {

			return pinField;
		}

		@Override
		public Component getLastComponent(Container container) {

			if (helpLabel != null && helpLabel.isVisible()) {

				return helpLabel;
			} else {

				return infoLabel;
			}
		}
	}

	public class AdvancedShowSigDataGUIFocusTraversalPolicy extends
			FocusTraversalPolicy {

		@Override
		public Component getComponentAfter(Container container,
				Component component) {

			if (component.equals(enterPINButton)) {

				return cancelButton;
			}

			if (component.equals(cancelButton)) {

				return infoLabel;
			}
			if (component.equals(infoLabel)) {

				if (helpLabel != null && helpLabel.isVisible()) {

					return helpLabel;
				} else {

					return null;
				}
			}

			if (component.equals(helpLabel)) {

				return null;
			}

			// default
			return enterPINButton;
		}

		@Override
		public Component getComponentBefore(Container container,
				Component component) {

			if (component.equals(enterPINButton)) {
				if (helpLabel != null && helpLabel.isVisible()) {

					return helpLabel;
				} else {

					return infoLabel;
				}
			}

			if (component.equals(cancelButton)) {

				return enterPINButton;
			}

			if (component.equals(infoLabel)) {

				return cancelButton;
			}

			if (component.equals(helpLabel)) {

				return infoLabel;
			}

			// default
			return enterPINButton;
		}

		@Override
		public Component getDefaultComponent(Container container) {

			return enterPINButton;
		}

		@Override
		public Component getFirstComponent(Container container) {

			return enterPINButton;
		}

		@Override
		public Component getLastComponent(Container container) {

			if (helpLabel != null && helpLabel.isVisible()) {

				return helpLabel;
			} else {

				return infoLabel;
			}
		}
	}
}
