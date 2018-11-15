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
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

@Component
public class ClientRunner implements CommandLineRunner {

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

        final File f = fileToUpload;
        client.upload(s -> {
            try {
                FileInputStream fileInputStream = new FileInputStream(f);
                DataInputStream inputStream = new DataInputStream(fileInputStream);

                while (inputStream.available() > 0) {
                    byte[] buf = new byte[128];
                    inputStream.read(buf);

                    s.onNext(FilePart.newBuilder()
                            .setPart(ByteString.copyFrom(buf))
                            .build());
                }

                s.onComplete();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
