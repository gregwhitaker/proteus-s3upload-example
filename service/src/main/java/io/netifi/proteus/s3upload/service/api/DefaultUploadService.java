package io.netifi.proteus.s3upload.service.api;

import com.amazonaws.services.s3.AmazonS3;
import io.netifi.proteus.s3upload.service.protobuf.Empty;
import io.netifi.proteus.s3upload.service.protobuf.FilePart;
import io.netifi.proteus.s3upload.service.protobuf.UploadService;
import io.netifi.proteus.s3upload.service.s3.S3WritableByteChannel;
import io.netty.buffer.ByteBuf;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.UUID;

@Component
public class DefaultUploadService implements UploadService {

    @Autowired
    private AmazonS3 s3client;

    @Value("s3.bucket")
    private String s3Bucket;

    @Override
    public Mono<Empty> upload(Publisher<FilePart> messages, ByteBuf metadata) {
        messages.subscribe(new Subscriber<FilePart>() {
            final String s3Key =  UUID.randomUUID().toString();

            private Subscription subscription;
            private S3WritableByteChannel s3Channel;

            @Override
            public void onSubscribe(Subscription s) {
                subscription = s;

                try {
                    s3Channel = new S3WritableByteChannel(s3client, s3Bucket, s3Key);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }

                subscription.request(1);
            }

            @Override
            public void onNext(FilePart filePart) {
                try {
                    s3Channel.write(filePart.getPart().asReadOnlyByteBuffer());
                    subscription.request(1);
                } catch (IOException e) {
                    try {
                        s3Channel.close();
                        subscription.cancel();
                    } catch (IOException ignore) {
                        // noop
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                throw new RuntimeException(t);
            }

            @Override
            public void onComplete() {
                try {
                    s3Channel.close();
                } catch (IOException ignore) {
                    // Noop
                }
            }
        });

        return Mono.empty();
    }
}
