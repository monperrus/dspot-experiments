/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.facebook.imagepipeline.producers;


import android.content.ContentResolver;
import android.net.Uri;
import com.facebook.common.memory.PooledByteBuffer;
import com.facebook.common.memory.PooledByteBufferFactory;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.testing.TestExecutorService;
import java.io.InputStream;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

import static Config.NONE;
import static LocalContentUriFetchProducer.PRODUCER_NAME;


/**
 * Basic tests for LocalContentUriFetchProducer
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = NONE)
public class LocalContentUriFetchProducerTest {
    private static final String PRODUCER_NAME = PRODUCER_NAME;

    @Mock
    public PooledByteBufferFactory mPooledByteBufferFactory;

    @Mock
    public ContentResolver mContentResolver;

    @Mock
    public Consumer<EncodedImage> mConsumer;

    @Mock
    public ImageRequest mImageRequest;

    @Mock
    public ProducerListener mProducerListener;

    @Mock
    public Exception mException;

    private TestExecutorService mExecutor;

    private SettableProducerContext mProducerContext;

    private final String mRequestId = "mRequestId";

    private Uri mContentUri;

    private LocalContentUriFetchProducer mLocalContentUriFetchProducer;

    private EncodedImage mCapturedEncodedImage;

    @Test
    public void testLocalContentUriFetchCancelled() {
        mLocalContentUriFetchProducer.produceResults(mConsumer, mProducerContext);
        mProducerContext.cancel();
        Mockito.verify(mProducerListener).onProducerStart(mRequestId, LocalContentUriFetchProducerTest.PRODUCER_NAME);
        Mockito.verify(mProducerListener).onProducerFinishWithCancellation(mRequestId, LocalContentUriFetchProducerTest.PRODUCER_NAME, null);
        Mockito.verify(mConsumer).onCancellation();
        mExecutor.runUntilIdle();
        Mockito.verifyZeroInteractions(mPooledByteBufferFactory);
    }

    @Test
    public void testFetchLocalContentUri() throws Exception {
        PooledByteBuffer pooledByteBuffer = Mockito.mock(PooledByteBuffer.class);
        Mockito.when(mPooledByteBufferFactory.newByteBuffer(ArgumentMatchers.any(InputStream.class))).thenReturn(pooledByteBuffer);
        Mockito.when(mContentResolver.openInputStream(mContentUri)).thenReturn(Mockito.mock(InputStream.class));
        mLocalContentUriFetchProducer.produceResults(mConsumer, mProducerContext);
        mExecutor.runUntilIdle();
        Assert.assertEquals(2, mCapturedEncodedImage.getByteBufferRef().getUnderlyingReferenceTestOnly().getRefCountTestOnly());
        Assert.assertSame(pooledByteBuffer, mCapturedEncodedImage.getByteBufferRef().get());
        Mockito.verify(mProducerListener).onProducerStart(mRequestId, LocalContentUriFetchProducerTest.PRODUCER_NAME);
        Mockito.verify(mProducerListener).onUltimateProducerReached(mRequestId, LocalContentUriFetchProducerTest.PRODUCER_NAME, true);
    }

    @Test(expected = RuntimeException.class)
    public void testFetchLocalContentUriFailsByThrowing() throws Exception {
        Mockito.when(mPooledByteBufferFactory.newByteBuffer(ArgumentMatchers.any(InputStream.class))).thenThrow(mException);
        Mockito.verify(mConsumer).onFailure(mException);
        Mockito.verify(mProducerListener).onProducerStart(mRequestId, LocalContentUriFetchProducerTest.PRODUCER_NAME);
        Mockito.verify(mProducerListener).onProducerFinishWithFailure(mRequestId, LocalContentUriFetchProducerTest.PRODUCER_NAME, mException, null);
        Mockito.verify(mProducerListener).onUltimateProducerReached(mRequestId, LocalContentUriFetchProducerTest.PRODUCER_NAME, false);
    }
}
