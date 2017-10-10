

package org.traccar.protocol;


public class AmplCarscopProtocolDecoderTest extends org.traccar.ProtocolTest {
    @org.junit.Test
    public void testDecode() throws java.lang.Exception {
        org.traccar.protocol.CarscopProtocolDecoder decoder = new org.traccar.protocol.CarscopProtocolDecoder(new org.traccar.protocol.CarscopProtocol());
        verifyNothing(decoder, text("*160618233129UB00HSO"));
        verifyNothing(decoder, text("*160618232614UD00232614A5009.1747N01910.3829E0.000160618298.2811000000L000000"));
        verifyNothing(decoder, text("*160618232529UB05CW9999C00000538232529A5009.1747N01910.3829E0.000160618298.2811000000L000000"));
        verifyPosition(decoder, text("*040331141830UB05123456789012345061825A2934.0133N10627.2544E000.0040331309.6200000000L000000"), position("2004-03-31 06:18:25.000", true, 29.56689, 106.45424));
        verifyPosition(decoder, text("*040331141830UB04999999984061825A2934.0133N10627.2544E000.0040331309.6200000000L000000"));
        verifyPosition(decoder, text("*040331141830UA012Hi-jack061825A2934.0133N10627.2544E000.0040331309.6200000000L000000"));
        verifyPosition(decoder, text("*150817160254UB05CC8011400042499160254A2106.8799S14910.2583E000.0150817158.3511111111L000000"));
    }
}
