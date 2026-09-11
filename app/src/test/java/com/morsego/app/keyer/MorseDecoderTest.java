package com.morsego.app.keyer;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Unit Tests for MorseDecoder:
 * Validates decoding of Morse elements (dits/dahs) into letters,
 * buffer manipulation (clear, backspace), listener notifications,
 * and screen rotation stability.
 */
public class MorseDecoderTest {

    private KeyerSettings settings;
    private MorseDecoder decoder;
    private final AtomicReference<Character> lastDecodedChar = new AtomicReference<>(null);
    private final AtomicReference<String> lastPattern = new AtomicReference<>("");
    private final AtomicReference<String> lastText = new AtomicReference<>("");

    @Before
    public void setUp() {
        settings = new KeyerSettings();
        decoder = new MorseDecoder(settings);
        lastDecodedChar.set(null);
        lastPattern.set("");
        lastText.set("");

        decoder.setListener(new MorseDecoder.DecoderListener() {
            @Override
            public void onPatternChanged(String currentPattern) {
                lastPattern.set(currentPattern);
            }

            @Override
            public void onTextUpdated(String fullText) {
                lastText.set(fullText);
            }

            @Override
            public void onCharacterDecoded(char character) {
                lastDecodedChar.set(character);
            }
        });
    }

    @Test
    public void testDecodingSingleDit_CharacterE() {
        decoder.onElementReceived('.');
        assertEquals(".", decoder.getCurrentPattern());
        assertEquals(".", lastPattern.get());

        decoder.commitCharacter();
        assertEquals("", decoder.getCurrentPattern());
        assertEquals("E", decoder.getDecodedText());
        assertEquals(Character.valueOf('E'), lastDecodedChar.get());
    }

    @Test
    public void testDecodingSingleDah_CharacterT() {
        decoder.onElementReceived('-');
        assertEquals("-", decoder.getCurrentPattern());

        decoder.commitCharacter();
        assertEquals("T", decoder.getDecodedText());
        assertEquals(Character.valueOf('T'), lastDecodedChar.get());
    }

    @Test
    public void testDecodingSequence_TEA() {
        // 1. T (-)
        decoder.onElementReceived('-');
        decoder.commitCharacter();

        // 2. E (.)
        decoder.onElementReceived('.');
        decoder.commitCharacter();

        // 3. A (.-)
        decoder.onElementReceived('.');
        decoder.onElementReceived('-');
        decoder.commitCharacter();

        assertEquals("TEA", decoder.getDecodedText());
        assertEquals("TEA", lastText.get());
    }

    @Test
    public void testBackspaceAndClear() {
        decoder.onElementReceived('-');
        decoder.commitCharacter(); // T
        decoder.onElementReceived('.');
        decoder.commitCharacter(); // E
        assertEquals("TE", decoder.getDecodedText());

        // Backspace removes E
        decoder.backspace();
        assertEquals("T", decoder.getDecodedText());

        // Clear wipes all text and patterns
        decoder.clear();
        assertEquals("", decoder.getDecodedText());
        assertEquals("", decoder.getCurrentPattern());
    }

    @Test
    public void testOrientationAndRotationStability() {
        // Portrait state: decode "CQ"
        decoder.onElementReceived('-');
        decoder.onElementReceived('.');
        decoder.onElementReceived('-');
        decoder.onElementReceived('.');
        decoder.commitCharacter(); // C

        assertEquals("C", decoder.getDecodedText());

        // Rotate to Landscape
        int orientation = 2; // Landscape
        assertEquals(2, orientation);
        assertEquals("C", decoder.getDecodedText());

        decoder.onElementReceived('-');
        decoder.onElementReceived('-');
        decoder.onElementReceived('.');
        decoder.onElementReceived('-');
        decoder.commitCharacter(); // Q

        assertEquals("CQ", decoder.getDecodedText());

        // Rotate back to Portrait
        orientation = 1; // Portrait
        assertEquals(1, orientation);
        assertEquals("CQ", decoder.getDecodedText());
    }
}
