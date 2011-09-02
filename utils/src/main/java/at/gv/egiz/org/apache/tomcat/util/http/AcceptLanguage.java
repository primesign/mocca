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


package at.gv.egiz.org.apache.tomcat.util.http;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Util to process the "Accept-Language" header. Used by facade to implement
 * getLocale() and by StaticInterceptor.
 *
 * Not optimized - it's very slow.
 * 
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author Harish Prabandham
 * @author costin@eng.sun.com
 */
public class AcceptLanguage {

    public static Locale getLocale(String acceptLanguage) {
        if( acceptLanguage == null ) return Locale.getDefault();

        Hashtable<String,Vector<String>> languages =
            new Hashtable<String,Vector<String>>();
        Vector<Double> quality = new Vector<Double>();
        processAcceptLanguage(acceptLanguage, languages, quality);

        if (languages.size() == 0) return Locale.getDefault();

        Vector<Locale> l = new Vector<Locale>();
        extractLocales( languages,quality, l);

        return (Locale)l.elementAt(0);
    }

    public static Enumeration<Locale> getLocales(String acceptLanguage) {
            // Short circuit with an empty enumeration if null header
        if (acceptLanguage == null) {
            Vector<Locale> v = new Vector<Locale>();
            v.addElement(Locale.getDefault());
            return v.elements();
        }
        
        Hashtable<String,Vector<String>> languages =
            new Hashtable<String,Vector<String>>();
        Vector<Double> quality=new Vector<Double>();
            processAcceptLanguage(acceptLanguage, languages , quality);

        if (languages.size() == 0) {
            Vector<Locale> v = new Vector<Locale>();
            v.addElement(Locale.getDefault());
            return v.elements();
        }
            Vector<Locale> l = new Vector<Locale>();
            extractLocales( languages, quality , l);
            return l.elements();
    }

    private static void processAcceptLanguage( String acceptLanguage,
            Hashtable<String,Vector<String>> languages, Vector<Double> q)
    {
        StringTokenizer languageTokenizer =
            new StringTokenizer(acceptLanguage, ",");

        while (languageTokenizer.hasMoreTokens()) {
            String language = languageTokenizer.nextToken().trim();
            int qValueIndex = language.indexOf(';');
            int qIndex = language.indexOf('q');
            int equalIndex = language.indexOf('=');
            Double qValue = new Double(1);

            if (qValueIndex > -1 &&
                    qValueIndex < qIndex &&
                    qIndex < equalIndex) {
                    String qValueStr = language.substring(qValueIndex + 1);
                language = language.substring(0, qValueIndex);
                qValueStr = qValueStr.trim().toLowerCase();
                qValueIndex = qValueStr.indexOf('=');
                qValue = new Double(0);
                if (qValueStr.startsWith("q") &&
                    qValueIndex > -1) {
                    qValueStr = qValueStr.substring(qValueIndex + 1);
                    try {
                        qValue = new Double(qValueStr.trim());
                    } catch (NumberFormatException nfe) {
                    }
                }
            }

            // XXX
            // may need to handle "*" at some point in time

            if (! language.equals("*")) {
                String key = qValue.toString();
                Vector<String> v;
                if (languages.containsKey(key)) {
                    v = languages.get(key) ;
                } else {
                    v= new Vector<String>();
                    q.addElement(qValue);
                }
                v.addElement(language);
                languages.put(key, v);
            }
        }
    }

    private static void extractLocales(Hashtable<String, Vector<String>> languages, Vector<Double> q,
            Vector<Locale> l)
    {
        // XXX We will need to order by q value Vector in the Future ?
        Enumeration<Double> e = q.elements();
        while (e.hasMoreElements()) {
            Vector<String> v =
                (Vector<String>)languages.get(((Double)e.nextElement()).toString());
            Enumeration<String> le = v.elements();
            while (le.hasMoreElements()) {
                    String language = (String)le.nextElement();
                        String country = "";
                        int countryIndex = language.indexOf("-");
                if (countryIndex > -1) {
                    country = language.substring(countryIndex + 1).trim();
                    language = language.substring(0, countryIndex).trim();
                }
                l.addElement(new Locale(language, country));
            }
        }
    }


}
