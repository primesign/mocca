/*******************************************************************************
 * <copyright> Copyright 2018 by PrimeSign GmbH, Graz, Austria </copyright>
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
 ******************************************************************************/

define(['libs/i18next.min', 'libs/LngDetector', 'text!lang/locale-en.json', 'text!lang/locale-de.json'], function (i18next, LngDetector, translationEN, translationDE) {

    var _log = log.getInstance('lang.js');

    i18next
        .use(LngDetector)
        .init({
            detection: {// Order of Language Detection, usually overriden with method call lang.setLocale()
                order: ['querystring', 'cookie', 'localStorage', 'navigator', 'htmlTag', 'path', 'subdomain'],
            },
            fallbackLng: 'en',
            debug: true,
            keySeparator: false,
            resources: {
                en: {
                    translation: JSON.parse(translationEN)
                },
                de: {
                    translation: JSON.parse(translationDE)
                }
            }
        });

    /**
     * Sets a locale or uses english locale if given locale cannot be found or parsed.
     * @param {string} locale the locale to set. Mustn't be null.
     */
    function setLocale(locale) {
        if (locale.length>=2) {
            i18next.changeLanguage(locale.substring(0,2));
        } else {
            locale='en';
        }
    }

    /**
     * Attempts to translate given message. If an error is caused then given message will be returned.
     * @param {string} message 
     * @returns the translated message.
     */
    function translate(message) {
        try {
            return i18next.t(message);
        } catch (error) {
            _log.debug('An error occured while trying to translate message: "' + message + '", error was: "' + error + '".');
            return message;
        }
    }

    return {
        setLocale : setLocale,
        translate: translate
    }

})