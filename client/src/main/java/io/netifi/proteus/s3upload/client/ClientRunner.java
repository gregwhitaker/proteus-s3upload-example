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
package io.netifi.proteus.s3upload.client;

import com.google.protobuf.ByteString;
import io.netifi.proteus.s3upload.service.protobuf.FilePart;
import io.netifi.proteus.s3upload.service.protobuf.UploadServiceClient;
import io.netifi.proteus.spring.core.annotation.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.CountDownLatch;

/**
 * Client that uploads a file to S3 via the upload service.
 */
@Component
public class ClientRunner implements CommandLineRunner {
    private static final Logger LOG = LoggerFactory.getLogger(ClientRunner.class);

    @Group("services.example.s3upload")
    private UploadServiceClient client;

    @Override
    public void run(String... args) throws Exception {
        File fileToUpload = null;
        if (0 < args.length) {
            fileToUpload = new File(args[0]);
        } else {
            throw new IllegalArgumentException("Missing fileToUpload argument");
        }

        CountDownLatch latch = new CountDownLatch(1);

        final File f = fileToUpload;
        client.upload(s -> {
            try {
                FileInputStream fileInputStream = new FileInputStream(f);
                DataInputStream inputStream = new DataInputStream(fileInputStream);

                int totalBytesWritten = 0;
                while (inputStream.available() > 0) {
                    byte[] buf = new byte[1024];
                    int bytesRead = inputStream.read(buf);
                    totalBytesWritten += bytesRead;

                    s.onNext(FilePart.newBuilder()
                            .setPart(ByteString.copyFrom(buf))
                            .build());

                    LOG.info("Writing Bytes: " + bytesRead);
                }

                s.onComplete();

                LOG.info("Upload Complete: {} bytes", totalBytesWritten);

                latch.countDown();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).subscribe();

        latch.await();
        System.exit(0);
    }
}
