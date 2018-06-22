define(['libs/i18next.min', 'libs/LngDetector', 'text!lang/locale-en.json', 'text!lang/locale-de.json'], function (i18next, LngDetector, translationEN, translationDE) {

    var _log = log.getInstance('lang.js');
    _log.debug('i18next: ' + i18next);

    //   i18next
    i18next
        .use(LngDetector)
        .init({
            detection: {// Order of Language Detection
                order: ['querystring', 'cookie', 'localStorage', 'navigator', 'htmlTag', 'path', 'subdomain'],
            },
            fallbackLng: 'de',
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

    function translate(message) {
        try {
            return i18next.t(message);
        } catch (error) {
            _log.debug('An error occured while trying to translate message: "' + message + '", error was: "' + error + '"');
            return message;
        }
    }

    return {
        translate: translate
    }

})