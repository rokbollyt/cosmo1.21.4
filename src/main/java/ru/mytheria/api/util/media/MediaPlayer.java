package ru.mytheria.api.util.media;


import com.google.common.io.BaseEncoding;
import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaInfo;
import dev.redstones.mediaplayerinfo.MediaPlayerInfo;
import net.minecraft.client.texture.AbstractTexture;
import ru.mytheria.api.util.render.RenderEngine;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.mytheria.api.clientannotation.QuickImport.mc;

public class MediaPlayer {

    private BufferedImage image;
    private AbstractTexture texture;
    private String title = "", artist = "", owner = "", lastTitle = "";
    private long duration = 0, position = 0;
    private boolean changeTrack;
    private IMediaSession session;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();


    private BufferedImage previousArtwork = null;
    private final Object lock = new Object();
    private String previousHash = "";


    public void onTick() {
        executor.submit(() -> {
            try {
                List<IMediaSession> currentSessions = MediaPlayerInfo.Instance.getMediaSessions();
                IMediaSession currentSession = currentSessions.stream()
                        .filter(s -> {
                            MediaInfo media = s.getMedia();
                            return media != null && (!media.getArtist().isEmpty() || !media.getTitle().isEmpty());
                        })
                        .findFirst()
                        .orElse(null);

                synchronized (lock) {
                    if (currentSession == null) {
                        clearState();
                        return;
                    }

                    MediaInfo info = currentSession.getMedia();
                    String newTitle = safe(info.getTitle());
                    String newArtist = safe(info.getArtist());
                    BufferedImage newArtwork = info.getArtwork();
                    long newDuration = info.getDuration();
                    long newPosition = info.getPosition();

                    boolean trackChanged = !Objects.equals(newTitle, lastTitle) || !Objects.equals(newArtist, artist);
                    boolean artworkChanged = !areImagesEqual(newArtwork, previousArtwork);

                    title = newTitle;
                    artist = newArtist;
                    duration = newDuration;
                    position = newPosition;
                    session = currentSession;
                    owner = currentSession.getOwner();

                    if (trackChanged) {
                        lastTitle = newTitle;
                        changeTrack = true;
                    }

                    if (changeTrack || artworkChanged) {
                        String newHash = (newArtwork != null) ? hashImage(newArtwork) : "";
                        if (!newHash.equals(previousHash)) {
                            AbstractTexture old = texture;
                            if (newArtwork != null) {
                                AbstractTexture newTexture = RenderEngine.convert(newArtwork);
                                texture = newTexture;
                                previousArtwork = newArtwork;
                                previousHash = newHash;
                            } else {
                                texture = null;
                                previousArtwork = null;
                                previousHash = "";
                            }


                            if (old != null) {
                                mc.getInstance().execute(old::close);
                            }
                        }

                        changeTrack = false;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                synchronized (lock) {
                    clearState();
                }
            }
        });
    }

    private void clearState() {
        title = "";
        artist = "";
        owner = "";
        lastTitle = "";
        duration = 0;
        position = 0;
        session = null;
        image = null;
        previousArtwork = null;
        previousHash = "";
        AbstractTexture old = texture;
        texture = null;
        if (old != null) {
            mc.getInstance().execute(old::close);
        }
    }

    private String safe(String s) {
        return s != null ? s : "";
    }


    private String hashImage(BufferedImage img) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            byte[] bytes = baos.toByteArray();
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(bytes);
            return BaseEncoding.base16().lowerCase().encode(digest);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private boolean areImagesEqual(BufferedImage img1, BufferedImage img2) {
        if (img1 == img2) return true;
        if (img1 == null || img2 == null) return false;
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) return false;

        for (int x = 0; x < img1.getWidth(); x += 10) {
            for (int y = 0; y < img1.getHeight(); y += 10) {
                if (img1.getRGB(x, y) != img2.getRGB(x, y)) return false;
            }
        }
        return true;
    }


    public String getLastTitle() {
        return lastTitle;
    }

    public AbstractTexture getTexture() {
        return texture;
    }

    public String getArtist() {
        return artist;
    }


    public boolean fullNullCheck() {
        synchronized (lock) {
            return session == null || texture == null || lastTitle.isEmpty();
        }
    }
}
