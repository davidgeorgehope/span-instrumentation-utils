package com.movieapi;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;

@RestController
@RequestMapping("/favorites")
public class ApiServlet {

    private static final Logger logger = LogManager.getLogger(ApiServlet.class);

    @Value("${TOGGLE_SERVICE_DELAY:0}")
    private Integer delayTime;

    @Value("${TOGGLE_CANARY_DELAY:0}")
    private Integer sleepTime;

    @Value("${TOGGLE_CANARY_FAILURE:0}")
    private double toggleCanaryFailure;

    // Hardcoded user favorites
    private static final Map<   String, String[]> userFavorites = new HashMap<>();

    @PostConstruct
    public void init() {
        userFavorites.put("user1", new String[]{"Inception", "The Matrix"});
        userFavorites.put("user2", new String[]{"Interstellar", "Moon"});
        userFavorites.put("user3", new String[]{"Blade Runner", "Dune"});
    }

    @GetMapping
    public String helloWorld(@RequestParam(required = false) String user_id) throws InterruptedException {
        if (user_id == null) {
            logger.info("Main request successful");
            return "Hello World!";
        } else {
            return getUserFavorites(user_id);
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String handlePost(@RequestParam String user_id, @RequestBody String requestBody) throws InterruptedException, Exception {
        handleDelay();
        String favorites = getUserFavorites(user_id);
        handleCanary();
        return favorites;
    }

    private String getUserFavorites(String user_id) throws InterruptedException {
        handleDelay();
        logger.info("Getting favorites for user " + user_id);

        String[] favorites = userFavorites.getOrDefault(user_id, new String[]{});
        JSONObject favoritesJson = new JSONObject();
        favoritesJson.put("favorites", favorites);

        String returnedFavorites = favoritesJson.toString();
        logger.info("User " + user_id + " has favorites " + returnedFavorites);
        return returnedFavorites;
    }

    private void handleDelay() throws InterruptedException {
        if (delayTime != null && delayTime > 0) {
            Random random = new Random();
            double randomGaussDelay = Math.min(delayTime * 5, Math.max(0, random.nextGaussian() * delayTime));
            TimeUnit.MILLISECONDS.sleep((long) randomGaussDelay);
        }
    }

    private void handleCanary() throws Exception {
        Random random = new Random();
        if (sleepTime > 0 && random.nextDouble() < 0.5) {
            double randomGaussDelay = Math.min(sleepTime * 5, Math.max(0, random.nextGaussian() * sleepTime));
            TimeUnit.MILLISECONDS.sleep((long) randomGaussDelay);
            logger.info("Canary enabled");

            if (random.nextDouble() < toggleCanaryFailure) {
                logger.error("Something went wrong");
                throw new Exception("Something went wrong");
            }
        }
    }
}
