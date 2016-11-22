package music.examples;

import music.*;
import music.midi.MusicPlayer;
import static music.Instrument.*;
import static music.MusicLanguage.*;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

public class RowYourBoatForever {

    public static void main(String[] args) throws MidiUnavailableException, InvalidMidiDataException {
        Music rowYourBoat =
            notes("C C C3/4 D/4 E |"
                + "E3/4 D/4 E3/4 F/4 G2 |"
                + "C'/3 C'/3 C'/3 G/3 G/3 G/3 E/3 E/3 E/3 C/3 C/3 C/3 |"
                + "G3/4 F/4 E3/4 D/4 C2",
                PIANO);
        
        int voices = 4;
        Music rowRound =
            canon(forever(rowYourBoat), rowYourBoat.duration()/voices, transposer(Pitch.OCTAVE), voices);
        
        MusicPlayer.play(rowRound);
    }
}
