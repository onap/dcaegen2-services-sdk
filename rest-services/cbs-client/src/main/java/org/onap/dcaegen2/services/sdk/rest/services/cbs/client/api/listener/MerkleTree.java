/*
 * ============LICENSE_START====================================
 * DCAEGEN2-SERVICES-SDK
 * =========================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
 * =========================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=====================================
 */

package org.onap.dcaegen2.services.sdk.rest.services.cbs.client.api.listener;

import io.vavr.Function1;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.onap.dcaegen2.services.sdk.rest.services.annotations.ExperimentalApi;

/**
 * An immutable <a href="https://en.wikipedia.org/wiki/Merkle_tree" target="_blank">Merkle Tree</a> implementation.
 *
 * Each node is labelled with a {@code String} label. A path of a node is defined as a list of labels from root to a
 * given node.
 *
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.2
 */
@ExperimentalApi
public final class MerkleTree<V> {

    private static final String DEFAULT_DIGEST_ALGORITHM = "SHA-256";
    private final ValueSerializer<V> valueSerializer;
    private final byte[] hash;
    private final Option<V> value;
    private final Map<String, MerkleTree<V>> children;
    private final HashAlgorithm hashAlgorithm;
    private final Function1<V, byte[]> hashForValue;

    private MerkleTree(
            @NotNull ValueSerializer<V> valueSerializer,
            @NotNull HashAlgorithm hashAlgorithm,
            @NotNull byte[] hash,
            @NotNull Option<V> value,
            @NotNull Map<String, MerkleTree<V>> children) {
        this.hashAlgorithm = Objects.requireNonNull(hashAlgorithm);
        this.valueSerializer = Objects.requireNonNull(valueSerializer);
        this.hash = Objects.requireNonNull(hash.clone());
        this.value = Objects.requireNonNull(value);
        this.children = Objects.requireNonNull(children);
        hashForValue = valueSerializer.andThen(hashAlgorithm);
    }

    /**
     * Create an empty tree with given serializer and using default digest algorithm as a hash function.
     *
     * @param serializer a way of serializing a value to array of bytes
     * @param <V>        type of values kept in a tree
     * @return empty tree
     */
    public static @NotNull <V> MerkleTree<V> emptyWithDefaultDigest(@NotNull ValueSerializer<V> serializer) {
        return emptyWithDigest(DEFAULT_DIGEST_ALGORITHM, serializer);
    }

    /**
     * Create an empty tree with given serializer and given digest algorithm used as a hash function.
     *
     * @param digestAlgorithmName name of a digest algorithm as used by {@link MessageDigest#getInstance(String)}
     * @param serializer          a way of serializing a value to array of bytes
     * @param <V>                 type of values kept in a tree
     * @return empty tree
     */
    public static @NotNull <V> MerkleTree<V> emptyWithDigest(
            @NotNull String digestAlgorithmName,
            @NotNull ValueSerializer<V> serializer) {
        return emptyWithHashProvider(serializer, messages -> {
            final MessageDigest messageDigest = messageDigest(digestAlgorithmName);
            messages.forEach(messageDigest::update);
            return messageDigest.digest();
        });
    }

    /**
     * Create an empty tree with given hash function.
     *
     * @param serializer    a function which serializes values to a byte array
     * @param hashAlgorithm a function which calculates a hash of a serialized value
     * @param <V>           type of values kept in a tree
     * @return empty tree
     */

    public static <V> MerkleTree<V> emptyWithHashProvider(ValueSerializer<V> serializer, HashAlgorithm hashAlgorithm) {
        return MerkleTree.empty(serializer, hashAlgorithm);
    }

    private static <V> MerkleTree<V> empty(@NotNull ValueSerializer<V> serializer, HashAlgorithm hashAlgorithm) {
        return new MerkleTree<>(serializer, hashAlgorithm, new byte[0], Option.none(), HashMap.empty());
    }

    private static MessageDigest messageDigest(String digestAlgorithmName) {
        try {
            return MessageDigest.getInstance(digestAlgorithmName);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Unsupported hash algorithm " + digestAlgorithmName, e);
        }
    }


    /**
     * Assigns a value to a given path.
     * <p>
     * Overrides current value if already exists.
     *
     * @param path  path of labels from root
     * @param value a value to assign
     * @return an updated tree instance or <code>this</code> if hashes are the same
     */

    public MerkleTree<V> add(List<String> path, V value) {
        return addSubtree(path, leaf(value));
    }

    private MerkleTree<V> addSubtree(final List<String> path, final MerkleTree<V> subtree) {
        if (path.isEmpty()) {
            return subtree;
        } else {
            String label = path.head();
            MerkleTree<V> newSubtree = children.get(label).fold(
                    () -> MerkleTree.empty(valueSerializer, hashAlgorithm).addSubtree(path.tail(), subtree),
                    node -> node.addSubtree(path.tail(), subtree)
            );
            return addSubtree(label, newSubtree);
        }
    }

    private MerkleTree<V> addSubtree(String label, MerkleTree<V> subtree) {
        final Map<String, MerkleTree<V>> newSubtrees = children.put(label, subtree);
        byte[] newHash = composeHashes(newSubtrees.iterator(this::hashForSubtree));
        return Arrays.equals(newHash, hash) ? this : new MerkleTree<>(
                valueSerializer,
                hashAlgorithm,
                newHash,
                value,
                newSubtrees
        );
    }

    private MerkleTree<V> leaf(V value) {
        return new MerkleTree<>(valueSerializer, hashAlgorithm, hashForValue.apply(value), Option.of(value), HashMap.empty());
    }

    /**
     * Returns a subtree with given node as a root.
     *
     * @param path a path of a node to be a subtree root
     * @return Some(subtree) if path exists, None otherwise
     */
    public Option<MerkleTree<V>> subtree(List<String> path) {
        return path.headOption().fold(
                () -> Option.of(this),
                head -> children.get(head).flatMap(subtree -> subtree.subtree(path.tail()))
        );
    }

    /**
     * Checks if nodes under given path are the same in {@code this} and {@code other} tree.
     *
     * @param other a tree to compare with
     * @param path  a path to a subtree to compare
     * @return true if hashes are the same, false otherwise
     */

    public boolean isSame(List<String> path, MerkleTree<V> other) {
        final byte[] oldHash = other.hashOf(path);
        final byte[] curHash = hashOf(path);
        return Arrays.equals(oldHash, curHash);
    }

    /**
     * Returns a hash of a node under given path.
     *
     * @param path a path of a node to check
     * @return a hash or empty array if node does not exist
     */

    public byte[] hashOf(List<String> path) {
        return subtree(path)
                .map(MerkleTree::hash)
                .getOrElse(() -> new byte[0]);
    }

    /**
     * Gets a value assigned to a given path.
     *
     * @param path to search for
     * @return Some(value) if path exists and contains a value, None otherwise
     */

    public Option<V> get(List<String> path) {
        return subtree(path).flatMap(MerkleTree::value);
    }

    byte[] hash() {
        return hash.clone();
    }

    private Option<V> value() {
        return value;
    }

    private byte[] hashForSubtree(String label, MerkleTree<V> subtree) {
        return composeHashes(List.of(label.getBytes(), subtree.hash()));
    }

    private byte[] composeHashes(Iterable<byte[]> hashes) {
        return hashAlgorithm.apply(hashes);
    }

    @Override
    public String toString() {
        return "(\"" + value.map(Object::toString).getOrElse("") + "\" ["
                + children.map(entry -> entry._1 + "=" + entry._2).mkString(", ")
                + "])";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MerkleTree<?> mtNode = (MerkleTree<?>) o;
        return Arrays.equals(hash, mtNode.hash);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(hash);
    }
}
