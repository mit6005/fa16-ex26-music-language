package music.examples;

import music.*;
import music.midi.MusicPlayer;
import static music.Instrument.*;
import static music.MusicLanguage.*;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

public class RowYourBoatTwice {

    public static void main(String[] args) throws MidiUnavailableException, InvalidMidiDataException {
        Music rowYourBoat =
            notes("C C C3/4 D/4 E |"
                + "E3/4 D/4 E3/4 F/4 G2 |"
                + "C'/3 C'/3 C'/3 G/3 G/3 G/3 E/3 E/3 E/3 C/3 C/3 C/3 |" 
                + "G3/4 F/4 E3/4 D/4 C2",
                PIANO);
        
        int beatsDelay = 4;
        Music rowRound =
            together(rowYourBoat, delay(rowYourBoat, beatsDelay));
        
        MusicPlayer.play(rowRound);
    }
}
