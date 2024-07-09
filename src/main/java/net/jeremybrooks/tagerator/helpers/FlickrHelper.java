/*
 * Tagerator is Copyright 2011 by Jeremy Brooks
 *
 * This file is part of Tagerator.
 *
 *  Tagerator is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Tagerator is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Tagerator.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.jeremybrooks.tagerator.helpers;


import com.github.scribejava.core.model.OAuth1RequestToken;
import net.jeremybrooks.jinx.Jinx;
import net.jeremybrooks.jinx.JinxConstants;
import net.jeremybrooks.jinx.OAuthAccessToken;
import net.jeremybrooks.jinx.api.PhotosApi;
import net.jeremybrooks.tagerator.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;


/**
 * This is a wrapper around the Flickr API library.
 *
 * <p>This wrapper provides access to the authentication methods, the API
 * interfaces, and metadata about the user.</p>
 *
 * <p>This class is implemented as a Singleton. Calling {@code FlickrHelper.getInstance()}
 * will return a reference to the instance of this class. The initialize
 * method must be called once before other methods are called.</p>
 *
 * @author Jeremy Brooks
 */
public class FlickrHelper {
    private static final Logger logger = LogManager.getLogger(FlickrHelper.class);
    private static FlickrHelper instance = null;

    private final Jinx jinx;

    /* File that holds oauth token info. */
    private final Path oauthTokenFile;

    private OAuthAccessToken oAuthAccessToken = null;

    private OAuth1RequestToken tempToken = null;

    /* Private constructor. This class is a Singleton. */
    private FlickrHelper() throws RuntimeException {
        this.oauthTokenFile = Paths.get(Main.configDir.toString(), "jinx_oauth.token");
        Properties secrets = new Properties();
        try {
            secrets.load(FlickrHelper.class.getClassLoader().getResourceAsStream("net/jeremybrooks/tagerator/private.properties"));
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to load secrets from class path.", ioe);
        }
        jinx = new Jinx(secrets.getProperty("FLICKR_KEY"),
                secrets.getProperty("FLICKR_SECRETS"), JinxConstants.OAuthPermissions.read);
    }


    /**
     * Get a reference to the only instance of this class.
     *
     * <p>The first time this method is called, an instance of this class will be created
     * and initialized. If that initialization fails, a RuntimeException will be thrown.
     * Normally this will happen during application startup, so the first time this method
     * is called, the caller should check for RuntimeException and notify the user of the
     * failure.</p>
     *
     * @return reference to FlickrHelper instance.
     */
    public static FlickrHelper getInstance() throws RuntimeException {
        if (instance == null) {
            instance = new FlickrHelper();
        }
        return instance;
    }

    /**
     * Authorize the user.
     *
     * <p>Authorization data is loaded from the token file if available. If the
     * authorization data is not available, this method returns false.</p>
     *
     * @return true if user is authorized.
     */
    public boolean authorize() {
        boolean success = false;
        // try loading oauth token
        if (Files.exists(oauthTokenFile)) {
            logger.info("Loading oauth token from {}", this.oauthTokenFile);
            oAuthAccessToken = new OAuthAccessToken();
            try (InputStream in = Files.newInputStream(oauthTokenFile)) {
                oAuthAccessToken.load(in);
                success = true;
            } catch (Exception e) {
                logger.warn("Unable to load oauth access token from file.", e);
            }
        }

        return success;
    }


    /**
     * Get the username of the currently authorized user.
     *
     * @return username of the currently authorized user.
     */
    public String getUsername() {
        return this.oAuthAccessToken == null ? null : this.oAuthAccessToken.getUsername();
    }


    /**
     * Get the NSID of the currently authorized user.
     *
     * @return NSID of the currently authorized user.
     */
    public String getNSID() {
        return this.oAuthAccessToken == null ? null : this.oAuthAccessToken.getNsid();
    }


    /**
     * Get the authentication URL.
     *
     * @return authentication URL.
     * @throws Exception if there are any errors.
     */
    public URL getAuthenticationURL() throws Exception {
        this.tempToken = jinx.getRequestToken();
        return URI.create(jinx.getAuthorizationUrl(this.tempToken)).toURL();
    }


    /**
     * Complete authentication.
     *
     * @param verificationCode oauth verification code.
     * @throws Exception if there are any errors.
     */
    public void completeAuthentication(String verificationCode) throws Exception {
        this.oAuthAccessToken = jinx.getAccessToken(tempToken, verificationCode);
        try (OutputStream out = Files.newOutputStream(oauthTokenFile)) {
            this.oAuthAccessToken.store(out);
        }
    }


    /**
     * Get an instance of the PhotosApi class that works with the currently authorized user.
     * @return new instance of the photosApi class.
     */
    public PhotosApi getPhotosApi() {
        return new PhotosApi(jinx);
    }


    /**
     * Delete all stored authorization data.
     */
    public void deauthorize() {
        if (Files.exists(oauthTokenFile)) {
            try {
                Files.delete(oauthTokenFile);
                logger.info("The oauth token has been deleted.");
            } catch (Exception e) {
                logger.warn("Could not delete the oauth token.");
            }
        }
    }
}
