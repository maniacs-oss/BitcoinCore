/**
 * Copyright 2013-2014 Ronald W Hoffman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ScripterRon.BitcoinCore;

import java.nio.ByteBuffer;

/**
 * The 'mempool' message requests a list of transactions in the peer memory pool.
 * The response is an 'inv' message listing the transactions in the pool.
 *
 * The message consists of just the message header.
 */
public class MempoolMessage {

    /**
     * Build a 'mempool' message
     *
     * @param       peer            Destination peer
     * @return                      'mempool' message
     */
    public static Message buildMempoolMessage(Peer peer) {
        ByteBuffer buffer = MessageHeader.buildMessage("mempool", new byte[0]);
        return new Message(buffer, peer, MessageHeader.MessageCommand.MEMPOOL);
    }

    /**
     * Process a 'mempool' message
     *
     * @param       msg             Message
     * @param       inBuffer        Input buffer
     * @param       msgListener     Message listener
     */
    public static void processMempoolMessage(Message msg, SerializedBuffer inBuffer, MessageListener msgListener) {
        msgListener.requestMemoryPool(msg.getPeer());
    }
}
