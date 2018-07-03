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