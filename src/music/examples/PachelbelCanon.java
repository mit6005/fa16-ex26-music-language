package music.examples;

import music.*;
import music.midi.MusicPlayer;
import static music.Instrument.*;
import static music.MusicLanguage.*;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

public class PachelbelCanon {
    public static void main(String[] args) throws MidiUnavailableException, InvalidMidiDataException {
        Music pachelbelBass =
            notes("D,2 A,,2 | B,,2 ^F,,2 | G,,2 D,,2 | G,,2 A,,2",
                  CELLO);
        // this is not the complete melody
        Music pachelbelMelody =
            notes("^F'2 E'2 | D'2 ^C'2 | B2 A2 | B2 ^C'2 |"
                + "D'2 ^C'2 | B2 A2 | G2 ^F2 | G2 E2 |"
                + "D ^F A G | ^F D ^F E | D B, D A | G B A G |"
                + "^F D E ^C' | D' ^F' A' A | B G A ^F | D D' D3/2 .1/2 |",
                  VIOLIN);
        int delay = 4 * 4; // each new voice enters after four 4-beat measures
        int voices = 3;
        Music pachelbelCanon =
            canon(forever(pachelbelMelody),
                  delay,
                  IDENTITY,
                  voices);
        Music pachelbel =
            concat(pachelbelBass, // bass line starts by itself
                   accompany(pachelbelCanon, // then joined by melody
                             pachelbelBass));
        MusicPlayer.play(pachelbel);
    }
}
