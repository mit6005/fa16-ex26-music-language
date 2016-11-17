package music.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import music.*;

/**
 * MusicPlayer can play a Music expression on the MIDI synthesizer.
 */
public class MusicPlayer {
    
    /**
     * Play music.
     * @param music music to play
     */
    public static void play(Music music) throws MidiUnavailableException, InvalidMidiDataException {
        final SequencePlayer player = new MidiSequencePlayer();
        
        // load the player with a sequence created from music (add a small delay at the beginning)
        final double warmup = 0.125;
        music.play(player, warmup);
        
        // start playing
        player.play();
    }
}
