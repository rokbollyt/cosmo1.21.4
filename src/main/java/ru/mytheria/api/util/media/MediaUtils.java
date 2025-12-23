package ru.mytheria.api.util.media;

import by.bonenaut7.mediatransport4j.api.MediaSession;
import by.bonenaut7.mediatransport4j.api.MediaTransport;
import com.google.common.io.BaseEncoding;
import net.minecraft.client.texture.AbstractTexture;
import ru.mytheria.api.util.render.RenderEngine;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MediaUtils {

    private static boolean initialized = false;
    private static final ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
    private static volatile MediaInfo mediaInfo = null;

    private static final Map<String, AbstractTexture> textureCache = new ConcurrentHashMap<>();
    private static String previousHash = "";

    public static class MediaInfo {
        public final String title;
        public final String artist;
        public final String textureHash;

        public MediaInfo(String title, String artist, String textureHash) {
            this.title = title;
            this.artist = artist;
            this.textureHash = textureHash;
        }

        public AbstractTexture getTexture() {
            return textureCache.get(textureHash);
        }
    }

    public static MediaInfo getCurrentMedia() {
        if (!initialized) {
            boolean bb = MediaTransport.init();
            initialized = true;
            System.out.println("АЛО ПРОСЫПАЙСЯ ИНИТ " + bb);

            s.scheduleAtFixedRate(() -> {
                try {
                    List<MediaSession> sessions = MediaTransport.getMediaSessions();
                    if (sessions != null && !sessions.isEmpty()) {
                        final MediaSession mediaSession = sessions.get(0);

                        String hash = "";
                        AbstractTexture texture = null;

                        if (mediaSession.hasThumbnail()) {
                            ByteBuffer buf = mediaSession.getThumbnail();
                            hash = hashBuffer(buf);

                            if (!hash.equals(previousHash)) {
                                AbstractTexture old = textureCache.remove(previousHash);
                                if (old != null) old.close();

                                texture = convertTexture(buf);
                                if (texture != null) {
                                    textureCache.put(hash, texture);
                                }
                                previousHash = hash;
                            }
                        }

                        mediaInfo = new MediaInfo(mediaSession.getTitle(), mediaSession.getArtist(), hash);
                    } else {
                        clearCache();
                        mediaInfo = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 0, 50, TimeUnit.MILLISECONDS);
        }
        return mediaInfo;
    }

    private static AbstractTexture convertTexture(ByteBuffer buffer) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(buffer.array()));
            if (img != null) {
                return RenderEngine.convert(img);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String hashBuffer(ByteBuffer buffer) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(buffer.array());
            return BaseEncoding.base16().lowerCase().encode(md.digest());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static void clearCache() {
        textureCache.values().forEach(AbstractTexture::close);
        textureCache.clear();
        previousHash = "";
    }
}
