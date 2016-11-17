package music.examples;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

import music.Instrument;
import music.Pitch;
import music.SequencePlayer;
import music.midi.MidiSequencePlayer;

public class ScaleSequence {
    
    /**
     * Play an octave up and back down starting from middle C.
     * 
     * addNote(instr, pitch, startBeat, numBeats) schedules a note with pitch value 'pitch'
     * played by 'instr' starting at 'startBeat' to be played for 'numBeats' beats.
     * 
     * For example:
     *     addNote(Instrument.PIANO, new Pitch('C'), 10, 1)
     * plays middle C at beat 10 for 1 beat.
     */
    public static void main(String[] args) throws MidiUnavailableException, InvalidMidiDataException {

        Instrument piano = Instrument.PIANO;

        // create a new player with 120 beats (quarter notes) per minute,
        // and 2 ticks per quarter note
        SequencePlayer player = new MidiSequencePlayer(120, 2);

        player.addNote(piano, new Pitch('C'), 0, 1);
        player.addNote(piano, new Pitch('D'), 1, 1);
        player.addNote(piano, new Pitch('E'), 2, 1);
        player.addNote(piano, new Pitch('F'), 3, 1);
        player.addNote(piano, new Pitch('G'), 4, 1);
        player.addNote(piano, new Pitch('A'), 5, 1);
        player.addNote(piano, new Pitch('B'), 6, 1);
        player.addNote(piano, new Pitch('C').transpose(Pitch.OCTAVE), 7, 1);
        player.addNote(piano, new Pitch('B'), 8, 1);
        player.addNote(piano, new Pitch('A'), 9, 1);
        player.addNote(piano, new Pitch('G'), 10, 1);
        player.addNote(piano, new Pitch('F'), 11, 1);
        player.addNote(piano, new Pitch('E'), 12, 1);
        player.addNote(piano, new Pitch('D'), 13, 1);
        player.addNote(piano, new Pitch('C'), 14, 1);

        System.out.println(player);

        // play!
        player.play();
    }
}
