package music.midi;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;

import javax.sound.midi.*;

import music.Instrument;
import music.Pitch;
import music.SequencePlayer;

/**
 * Schedules and plays a sequence of notes using the MIDI synthesizer.
 */
public class MidiSequencePlayer implements SequencePlayer {

    /**
     * Default tempo.
     */
    public static final int DEFAULT_BEATS_PER_MINUTE = 120;
    /**
     * Default MIDI ticks per beat.
     */
    public static final int DEFAULT_TICKS_PER_BEAT = 64;

    // the volume
    private static final int DEFAULT_VELOCITY = 100;

    // the generic marker meta message type
    private static final int META_MARKER = 6;
    // the "end_of_track" meta message type
    private static final int META_END_OF_TRACK = 47;

    private final Synthesizer synthesizer;

    // active MIDI channels, assigned to instruments
    private final Map<Instrument, Integer> channels = new HashMap<>();

    // next available channel number (not assigned to an instrument yet)
    private int nextChannel = 0;

    private final Sequencer sequencer;
    private final Track track;
    private final int beatsPerMinute;
    private final int ticksPerBeat;

    // event callback functions
    private final SortedMap<Integer, Consumer<Double>> callbacks = new TreeMap<>();

    /*
     * Rep invariant:
     *   sequencer and track are non-null,
     *   beatsPerMinute and ticksPerBeat are positive,
     *   channels and callbacks are non-null,
     *   channels does not contain value nextChannel
     */

    private void checkRep() {
        assert sequencer != null : "sequencer should be non-null";
        assert track != null : "track should be non-null";
        assert beatsPerMinute >= 0 : "should be positive number of beats per minute";
        assert ticksPerBeat >= 0 : "should be positive number of ticks per beat";
        assert callbacks != null : "callbacks should be non-null";
        assert callbacks != null : "callbacks should be non-null";
        assert ! channels.values().contains(nextChannel) : "nextChannel should not be assigned";
    }

    /**
     * Make a new MIDI sequence player with the default parameters.
     * @throws MidiUnavailableException
     * @throws InvalidMidiDataException
     */
    public MidiSequencePlayer() throws MidiUnavailableException, InvalidMidiDataException {
        this(DEFAULT_BEATS_PER_MINUTE, DEFAULT_TICKS_PER_BEAT);
    }

    /**
     * Make a new MIDI sequence player.
     * @param beatsPerMinute the number of beats per minute
     * @param ticksPerBeat the number of ticks per beat; every note plays for an integer number of ticks
     * @throws MidiUnavailableException
     * @throws InvalidMidiDataException
     */
    public MidiSequencePlayer(int beatsPerMinute, int ticksPerBeat)
            throws MidiUnavailableException, InvalidMidiDataException {
        synthesizer = MidiSystem.getSynthesizer();
        synthesizer.open();
        synthesizer.loadAllInstruments(synthesizer.getDefaultSoundbank());

        this.sequencer = MidiSystem.getSequencer();

        // create a sequence object with with tempo-based timing, where
        // the resolution of the time step is based on ticks per quarter note
        Sequence sequence = new Sequence(Sequence.PPQ, ticksPerBeat);
        this.beatsPerMinute = beatsPerMinute;
        this.ticksPerBeat = ticksPerBeat;

        // create an empty track; notes will be added to this track
        this.track = sequence.createTrack();

        sequencer.setSequence(sequence);

        checkRep();
    }

    /**
     * Schedule a note to be played with MIDI synthesizer.
     */
    @Override
    public void addNote(Instrument instr, Pitch pitch, double startBeat, double numBeats) {
        int channel = getChannel(instr);
        int note = getMidiNote(pitch);
        try {
            // schedule two events in the track, one for starting a note and
            // the other for ending the note.
            addMidiNoteEvent(ShortMessage.NOTE_ON, channel, note, (int) (startBeat * ticksPerBeat));
            addMidiNoteEvent(ShortMessage.NOTE_OFF, channel, note, (int) ((startBeat + numBeats) * ticksPerBeat));
        } catch (InvalidMidiDataException imde) {
            String msg = MessageFormat.format("Cannot add note with the pitch {0} at beat {1} " +
                                              "for duration {2}", note, startBeat, numBeats);
            throw new RuntimeException(msg, imde);
        }
    }

    /**
     * Schedule a MIDI note event.
     * @param eventType valid MidiMessage type in ShortMessage
     * @param channel valid channel
     * @param note valid pitch value
     * @param tick tick >= 0
     * @throws InvalidMidiDataException
     */
    private void addMidiNoteEvent(int eventType, int channel, int note, int tick) throws InvalidMidiDataException {
        ShortMessage msg = new ShortMessage(eventType, channel, note, DEFAULT_VELOCITY);
        this.track.add(new MidiEvent(msg, tick));
    }
    
    /**
     * Schedule a callback when the synthesizer reaches a time.
     */
    public void addEvent(Consumer<Double> callback, double atBeat) {
        int callbackNumber = saveCallback(callback);
        try {
            addMidiMetaEvent(callbackNumber, (int) (atBeat * ticksPerBeat));
        } catch (InvalidMidiDataException imde) {
            throw new RuntimeException("Cannot add event at beat " + atBeat, imde);
        };
    }
    
    private int saveCallback(Consumer<Double> callback) {
        int key = callbacks.isEmpty() ? 0 : callbacks.lastKey() + 1;
        callbacks.put(key, callback);
        checkRep();
        return key;
    }
    
    /**
     * Schedule a MIDI meta event
     * @param callback active callback number
     * @param tick tick >= 0
     * @throws InvalidMidiDataException
     */
    private void addMidiMetaEvent(int callback, int tick) throws InvalidMidiDataException {
        byte[] bytes = BigInteger.valueOf(callback).toByteArray();
        MetaMessage msg = new MetaMessage(META_MARKER, bytes, bytes.length);
        this.track.add(new MidiEvent(msg, tick));
    }

    /**
     * Open the MIDI sequencer and play the scheduled music.
     */
    @Override
    public void play() {
        try {
            sequencer.open();
        } catch (MidiUnavailableException mue) {
            throw new RuntimeException("Unable to open MIDI sequencer", mue);
        }
        sequencer.setTempoInBPM(this.beatsPerMinute);

        sequencer.addMetaEventListener(new MetaEventListener() {
            public void meta(MetaMessage meta) {
                if (meta.getType() == META_MARKER) {
                    // trigger event callback
                    int callbackNumber = new BigInteger(meta.getData()).intValue();
                    callbacks.remove(callbackNumber).accept(sequencer.getTickPosition() / (double)ticksPerBeat);
                } else if (meta.getType() == META_END_OF_TRACK) {
                    // allow the sequencer to finish
                    try { Thread.sleep(1000); } catch (InterruptedException ie) { }
                    // stop & close the sequencer
                    sequencer.stop();
                    sequencer.close();
                }
            }
        });

        // start playing!
        sequencer.start();
    }
    
    /**
     * Get a MIDI channel for the given instrument, allocating one if necessary.
     * @param instr instrument
     * @return channel for the instrument
     */
    private int getChannel(Instrument instr) {
        // check whether this instrument already has a channel
        if (channels.containsKey(instr)) {
            return channels.get(instr);
        }
        
        int channel = allocateChannel();
        patchInstrumentIntoChannel(channel, instr);
        channels.put(instr, channel);
        checkRep();
        return channel;
    }

    /**
     * @return next available channel number
     */
    private int allocateChannel() {
        MidiChannel[] channels = synthesizer.getChannels();
        if (nextChannel >= channels.length) {
            throw new RuntimeException("Tried to use too many instruments: limited to " + channels.length);
        }
        return nextChannel++;
    }
    
    private void patchInstrumentIntoChannel(int channel, Instrument instr) {
        try {
            addMidiNoteEvent(ShortMessage.PROGRAM_CHANGE, channel, instr.ordinal(), 0);
        } catch (InvalidMidiDataException imde) {
            throw new RuntimeException("Cannot set instrument", imde);
        }
    }

    /**
     * @return the MIDI note number for a pitch, defined as the number of
     *         semitones above C 5 octaves below middle C; for example,
     *         middle C is note 60
     */
    private static int getMidiNote(Pitch pitch) {
        return pitch.difference(Pitch.MIDDLE_C) + 60;
    }
    
    /**
     * @return a string that displays the entire track information as a
     *         sequence of MIDI events, where each event is either turning on
     *         or off a note at a certain tick, a marker event, or the end of
     *         the track
     */
    @Override
    public String toString() {
        String trackInfo = "";

        for (int i = 0; i < track.size(); i++) {
            final MidiEvent e = track.get(i);
            final MidiMessage msg = e.getMessage();
            final String msgString;

            if (msg instanceof ShortMessage) {
                final ShortMessage smg = (ShortMessage) msg;
                final int command = smg.getCommand();
                final String commandName;

                if (command == ShortMessage.NOTE_OFF) {
                    commandName = "NOTE_OFF";
                } else if (command == ShortMessage.NOTE_ON) {
                    commandName = "NOTE_ON ";
                } else {
                    commandName = "Unknown command " + command;
                }

                msgString = "Event: " + commandName + " Pitch: " + smg.getData1() + " ";

            } else if (msg instanceof MetaMessage) {
                final MetaMessage mmg = (MetaMessage) msg;
                final int type = mmg.getType();
                final String typeName;

                if (type == META_MARKER) {
                    typeName = "MARKER";
                } else if (type == META_END_OF_TRACK) {
                    typeName = "END_OF_TRACK";
                } else {
                    typeName = "Unknown type " + type;
                }

                msgString = "Meta event: " + typeName;

            } else {
                msgString = "Unknown event";
            }

            trackInfo += msgString + " Tick: " + e.getTick() + "\n";
        }

        return trackInfo;
    }
}
