package br.com.fiap.soat1.t32;

import br.com.fiap.soat1.t32.models.TokenDTO;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent;
import com.amazonaws.util.Base64;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.metrics.Metrics;
import software.amazon.lambda.powertools.tracing.Tracing;

import java.io.IOException;

import static java.util.Objects.isNull;
import static software.amazon.lambda.powertools.tracing.CaptureMode.DISABLED;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayCustomAuthorizerEvent, Boolean> {
    Logger log = LogManager.getLogger(App.class);

    @Logging(logEvent = true)
    @Tracing(captureMode = DISABLED)
    @Metrics(captureColdStart = true)
    public Boolean handleRequest(final APIGatewayCustomAuthorizerEvent input, final Context context) {

        try {
            final var authorization = getTokenObject(input);

            if(isNull(authorization) ||
                StringUtils.isNullOrEmpty(authorization.getDocument()) ||
                StringUtils.isNullOrEmpty(authorization.getExpiresAt())) {
                return Boolean.FALSE;
            }

        } catch (IOException e) {
            return Boolean.FALSE;
        }

        return  Boolean.TRUE;
    }

    private TokenDTO getTokenObject(final APIGatewayCustomAuthorizerEvent input) throws IOException {
        final var headers = input.getHeaders();

        if(headers.isEmpty()) {
            return null;
        }

        final var authorizationHeader = headers.get(HttpHeaders.AUTHORIZATION);

        if(isNull(authorizationHeader)) {
            return null;
        }

        return new ObjectMapper().readValue(Base64.decode(authorizationHeader), TokenDTO.class);
    }
}