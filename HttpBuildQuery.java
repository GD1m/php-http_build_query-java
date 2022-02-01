package ru.gdim.php.httpbuildquery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HttpBuildQuery {

    private static final String DELIMITER = "&";
    private static final String EQUALS = "=";
    private static final String OPENING_BRACE = "[";
    private static final String CLOSING_BRACE = "]";
    private static final String EMPTY_STRING = "";

    /**
     * Generate URL query string from Map of params. Nested Maps and Collections is also supported
     * <p>
     * PHP http_build_query() function analog
     *
     * @param params Map of params
     * @return URL query string like: foo=bar&amp;user[name]=Bob&amp;user[children][0][name]=Bobby&amp;user[children][1][name]=John
     */
    public static String httpBuildQuery(Map<Object, Object> params) {

        List<String> rootKeyChain = new ArrayList<>();

        StringBuilder urlQuery = processMap(rootKeyChain, params);

        if (urlQuery.length() > 0) {

            removeEndParamDelimiter(urlQuery);

        }

        return urlQuery.toString();

    }

    @SuppressWarnings("unchecked")
    private static StringBuilder processParam(List<String> keyChain, Object value) {

        if (value instanceof Map) {

            return processMap(keyChain, (Map<Object, Object>) value);

        }

        if (value instanceof Collection) {

            return processCollection(keyChain, (Collection<Object>) value);

        }

        return processFinalParam(keyChain, value);

    }

    private static StringBuilder processMap(List<String> keyChain, Map<Object, Object> params) {

        StringBuilder result = new StringBuilder();

        for (Map.Entry<Object, Object> entry : params.entrySet()) {

            String key = Objects.toString(entry.getKey(), null);
            Object value = entry.getValue();

            appendProcessedParam(result, keyChain, key, value);

        }

        return result;

    }

    private static StringBuilder processCollection(List<String> keyChain, Collection<Object> params) {

        StringBuilder result = new StringBuilder();

        if (!(params instanceof List)) {

            params = new ArrayList<>(params);

        }

        List<Object> paramList = (List<Object>) params;

        for (int paramIndex = 0; paramIndex < paramList.size(); paramIndex++) {

            String key = Integer.toString(paramIndex);
            Object value = paramList.get(paramIndex);

            appendProcessedParam(result, keyChain, key, value);

        }

        return result;

    }

    private static StringBuilder processFinalParam(List<String> keyChain, Object value) {

        return new StringBuilder()
                .append(encodeKeyChain(keyChain))
                .append(EQUALS)
                .append(encodeValue(value))
                .append(DELIMITER);

    }

    private static void appendProcessedParam(StringBuilder result, List<String> keyChain, String key, Object value) {

        StringBuilder processedParam = processParam(
                prepareNewKeyChain(keyChain, key),
                value
        );

        result.append(processedParam);

    }

    private static List<String> prepareNewKeyChain(List<String> keyChain, String currentKey) {

        List<String> keyChainCopy = new ArrayList<>(keyChain);
        keyChainCopy.add(currentKey);

        return keyChainCopy;

    }

    private static StringBuilder encodeKeyChain(List<String> keyChain) {

        StringBuilder result = new StringBuilder();

        boolean isFirst = true;

        for (String rawKey : keyChain) {

            String key = encodeValue(rawKey);

            if (isFirst) {

                result.append(key);

                isFirst = false;

            } else {

                result
                        .append(OPENING_BRACE)
                        .append(key)
                        .append(CLOSING_BRACE);

            }

        }

        return result;

    }

    private static String encodeValue(Object value) {

        return Objects.toString(value, EMPTY_STRING);

    }

    private static void removeEndParamDelimiter(StringBuilder urlQuery) {

        urlQuery.deleteCharAt(urlQuery.length() - 1);

    }

}
