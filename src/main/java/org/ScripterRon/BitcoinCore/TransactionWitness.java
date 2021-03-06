/*
 * Copyright 2016 Ronald W Hoffman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ScripterRon.BitcoinCore;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>A transaction witness has the following format:</p>
 * <pre>
 *   Size           Field               Description
 *   ===            =====               ===========
 *   VarInt         WitnessCount        Number of data elements
 *   Variable       WitnessList         Data elements
 * </pre>
 */
public class TransactionWitness implements ByteSerializable {

    /** Parent transaction */
    private final Transaction tx;

    /** Transaction input index */
    private final int txIndex;

    /** Witness data */
    private final List<byte[]> txWitness;

    /**
     * Create the transaction witness for the specified transaction index
     *
     * @param   tx                  Transaction
     * @param   txIndex             Transaction index
     */
    public TransactionWitness(Transaction tx, int txIndex) {
        this.tx = tx;
        this.txIndex = txIndex;
        this.txWitness = new ArrayList<>(2);
    }

    /**
     * Create a transaction witness from the encoded byte stream
     *
     * @param       tx                      Parent transaction
     * @param       txIndex                 Transaction input index
     * @param       inBuffer                Input buffer
     * @throws      EOFException            Input stream is too short
     * @throws      VerificationException   Verification error
     */
    public TransactionWitness(Transaction tx, int txIndex, SerializedBuffer inBuffer)
                                            throws EOFException, VerificationException {
        this.tx = tx;
        this.txIndex = txIndex;
        int count = inBuffer.getVarInt();
        this.txWitness = new ArrayList<>(count);
        for (int i=0; i<count; i++) {
            txWitness.add(inBuffer.getBytes());
        }
    }

    /**
     * Return the serialized transaction witness
     *
     * @param       outBuffer       Output buffer
     * @return                      Output buffer
     */
    @Override
    public SerializedBuffer getBytes(SerializedBuffer outBuffer) {
        outBuffer.putVarInt(txWitness.size());
        if (!txWitness.isEmpty()) {
            txWitness.forEach((elem) -> outBuffer.putVarInt(elem.length).putBytes(elem));
        }
        return outBuffer;
    }

    /**
     * Returns the serialized transaction input
     *
     * @return                      Serialized transaction input
     */
    @Override
    public byte[] getBytes() {
        SerializedBuffer buffer = new SerializedBuffer();
        return getBytes(buffer).toByteArray();
    }

    /**
     * Return the transaction containing this witness
     *
     * @return                      Parent transaction
     */
    public Transaction getTransaction() {
        return tx;
    }

    /**
     * Return the index of this input within the transaction inputs
     *
     * @return                      Transaction input index
     */
    public int getIndex() {
        return txIndex;
    }

    /**
     * Return the witness data
     *
     * @return                      Witness data
     */
    public List<byte[]> getWitness() {
        return txWitness;
    }

    /**
     * Build the ScriptSig from the witness data
     *
     * @return                      ScriptSig
     */
    public byte[] getScriptSig() {
        //
        // Calculate the total length
        //
        int length = 0;
        for (byte[] bytes : txWitness) {
            if (bytes.length < ScriptOpCodes.OP_PUSHDATA1) {
                length++;
            } else if (bytes.length < 256) {
                length += 2;
            } else if (bytes.length < 65536) {
                length += 3;
            } else {
                length += 5;
            }
            length += bytes.length;
        }
        //
        // Create the script
        //
        byte[] scriptBytes = new byte[length];
        int offset = 0;
        for (byte[] bytes : txWitness) {
            if (bytes.length < ScriptOpCodes.OP_PUSHDATA1) {
                scriptBytes[offset] = (byte)bytes.length;
                System.arraycopy(bytes, 0, scriptBytes, offset+1, bytes.length);
                offset += 1 + bytes.length;
            } else if (bytes.length < 256) {
                scriptBytes[offset] = (byte)ScriptOpCodes.OP_PUSHDATA1;
                scriptBytes[offset+1] = (byte)bytes.length;
                System.arraycopy(bytes, 0, scriptBytes, offset+2, bytes.length);
                offset += 2 + bytes.length;
            } else if (bytes.length < 65536) {
                scriptBytes[offset] = (byte)ScriptOpCodes.OP_PUSHDATA2;
                scriptBytes[offset+1] = (byte)bytes.length;
                scriptBytes[offset+2] = (byte)(bytes.length>>8);
                System.arraycopy(bytes, 0, scriptBytes, offset+3, bytes.length);
                offset += 3 + bytes.length;
            } else {
                scriptBytes[offset] = (byte)ScriptOpCodes.OP_PUSHDATA4;
                scriptBytes[offset+1] = (byte)bytes.length;
                scriptBytes[offset+2] = (byte)(bytes.length>>8);
                scriptBytes[offset+3] = (byte)(bytes.length>>16);
                scriptBytes[offset+4] = (byte)(bytes.length>>24);
                System.arraycopy(bytes, 0, scriptBytes, offset+5, bytes.length);
                offset += 5 + bytes.length;
            }
        }
        return scriptBytes;
    }
}
