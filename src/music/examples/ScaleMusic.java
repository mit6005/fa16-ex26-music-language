package music.examples;

import static music.Instrument.PIANO;
import static music.MusicLanguage.notes;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import music.Music;
import music.midi.MusicPlayer;

public class ScaleMusic {

    /**
     * Play an octave up and back down starting from middle C.
     */
    public static void main(String[] args) throws MidiUnavailableException, InvalidMidiDataException {
        
        // parse simplified abc into a Music
        Music scale = notes("C D E F G A B C' B A G F E D C", PIANO);
        
        System.out.println(scale);
        
        // play!
        MusicPlayer.play(scale);
    }
}
