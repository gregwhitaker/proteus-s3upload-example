/**
 * Copyright 2018 Netifi Inc.
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
package io.netifi.proteus.s3upload.service.api;

import com.amazonaws.services.s3.AmazonS3;
import io.netifi.proteus.s3upload.service.protobuf.Empty;
import io.netifi.proteus.s3upload.service.protobuf.FilePart;
import io.netifi.proteus.s3upload.service.protobuf.UploadService;
import io.netifi.proteus.s3upload.service.s3.S3WritableByteChannel;
import io.netty.buffer.ByteBuf;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.UUID;

/**
 * Service that writes to S3.
 */
@Component
public class DefaultUploadService implements UploadService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultUploadService.class);

    @Autowired
    private AmazonS3 s3client;

    @Value("${s3.bucket}")
    private String s3Bucket;

    @Override
    public Mono<Empty> upload(Publisher<FilePart> messages, ByteBuf metadata) {
        final String s3Key =  UUID.randomUUID().toString();

        try {
            final S3WritableByteChannel s3Channel = new S3WritableByteChannel(s3client, s3Bucket, s3Key);

            return Flux.from(messages)
                    .map(filePart -> {
                        try {
                            return s3Channel.write(filePart.getPart().asReadOnlyByteBuffer());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .doOnComplete(() -> {
                        try {
                            LOG.info("Upload Complete: " + s3Key);
                            s3Channel.close();
                        } catch (IOException e) {
                            // Noop
                        }
                    })
                    .then(Mono.just(Empty.getDefaultInstance()));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
