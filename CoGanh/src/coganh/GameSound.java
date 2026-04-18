package coganh;

import java.io.BufferedInputStream;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class GameSound {
	private AudioInputStream audio;
	public Clip clip;
	private boolean start = false;

	public GameSound() {
		try {
			InputStream raw = getClass().getResourceAsStream("sound.wav");
			BufferedInputStream bufferedIn = new BufferedInputStream(raw);
			audio = AudioSystem.getAudioInputStream(bufferedIn);
		} catch (Exception e1) {
			e1.printStackTrace();
			return;
		}
		try {
			clip = AudioSystem.getClip();
			clip.open(audio);
			// clip.loop(Clip.LOOP_CONTINUOUSLY);
		} catch (Exception e) {
			System.out.println("Exception");
		}
	}

	public void batDau() {
		if (clip == null) return; // Không crash khi sound.wav không tải được
		if (!start) {
			clip.loop(Clip.LOOP_CONTINUOUSLY);
			clip.start();
			start = true;
		}
	}

	public void stopMusic() {
		if (clip == null) return; // Không crash khi sound.wav không tải được
		start = false;
		clip.stop();
	}

}
