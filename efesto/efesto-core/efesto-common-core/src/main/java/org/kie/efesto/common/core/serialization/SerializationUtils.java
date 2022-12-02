package org.kie.efesto.common.core.serialization;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

import static org.kie.efesto.common.api.identifiers.LocalUri.SLASH;

public class SerializationUtils {

    public static String decodedPath(String toDecode) {
        StringTokenizer tok = new StringTokenizer(toDecode, SLASH);
        StringBuilder builder = new StringBuilder();
        while (tok.hasMoreTokens()) {
            builder.append(SLASH);
            builder.append(decodeString(tok.nextToken()));
        }
        return builder.toString();
    }

    public static String decodeString(String toDecode) {
        return URLDecoder.decode(toDecode, StandardCharsets.UTF_8);
    }

}
