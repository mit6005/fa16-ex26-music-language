package music.examples;

import music.*;
import music.midi.MusicPlayer;
import static music.Instrument.*;
import static music.MusicLanguage.*;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

public class RowYourBoatInitial {

    public static void main(String[] args) throws MidiUnavailableException, InvalidMidiDataException {
        Music rowYourBoat =
            notes("C C C3/4 D/4 E |"       // Row, row, row your boat,
                + "E3/4 D/4 E3/4 F/4 G2 |" // Gently down the stream.
                + "C'/3 C'/3 C'/3 G/3 G/3 G/3 E/3 E/3 E/3 C/3 C/3 C/3 |"
                                           // Merrily, merrily, merrily, merrily,
                + "G3/4 F/4 E3/4 D/4 C2",  // Life is but a dream.
                PIANO);
        
        System.out.println(rowYourBoat);
        
        MusicPlayer.play(rowYourBoat);
    }
}
