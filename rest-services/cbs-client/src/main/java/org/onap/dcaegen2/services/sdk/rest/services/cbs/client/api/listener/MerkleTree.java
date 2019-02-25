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

/**
 * An immutable <a href="https://en.wikipedia.org/wiki/Merkle_tree" target="_blank">Merkle Tree</a> implementation.
 * <p>
 * Each node is labelled with a {@code String} label. A path of a node is defined as a list of labels from root to a
 * given node.
 *
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.2
 */
public final class MerkleTree<V> {

    private static final String DEFAULT_DIGEST_ALGORITHM = "SHA-256";
    private final ValueSerializer<V> valueSerializer;
    private final HashAlgorithm hashAlgorithm;
    private final MTNode<V> root;
    private final Function1<V, byte[]> hashForValue;

    private MerkleTree(
            @NotNull ValueSerializer<V> valueSerializer,
            @NotNull HashAlgorithm hashAlgorithm,
            @NotNull MTNode<V> root) {
        this.valueSerializer = Objects.requireNonNull(valueSerializer);
        this.hashAlgorithm = Objects.requireNonNull(hashAlgorithm);
        this.root = Objects.requireNonNull(root);
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
        return new MerkleTree<>(serializer, hashAlgorithm, MTNode.empty(hashAlgorithm));
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
     * @param value a value to assign
     * @param path  path of labels from root
     * @return an updated tree instance or <code>this</code> if hashes are the same
     */
    public MerkleTree<V> add(V value, String... path) {
        return add(List.of(path), value);
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
        final MTNode<V> result = root.addChild(path, MTNode.leaf(hashAlgorithm, hashForValue.apply(value), value));
        return Arrays.equals(result.hash(), root.hash())
                ? this
                : new MerkleTree<>(valueSerializer, hashAlgorithm, result);
    }


    /**
     * Gets a value assigned to a given path.
     *
     * @param path to search for
     * @return Some(value) if path exists and contains a value, None otherwise
     */
    public Option<V> get(String... path) {
        return get(List.of(path));
    }

    /**
     * Gets a value assigned to a given path.
     *
     * @param path to search for
     * @return Some(value) if path exists and contains a value, None otherwise
     */
    public Option<V> get(List<String> path) {
        return root.findNode(path).flatMap(MTNode::value);
    }

    /**
     * Checks if nodes under given path are the same in {@code this} and {@code other} tree.
     *
     * @param other a tree to compare with
     * @param path  a path to a subtree to compare
     * @return true if hashes are the same, false otherwise
     */
    public boolean isSame(MerkleTree<V> other, String... path) {
        return isSame(List.of(path), other);
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
        return root
                .findNode(path)
                .map(node -> node.hash().clone())
                .getOrElse(() -> new byte[0]);
    }

    /**
     * Returns a hash of a node under given path.
     *
     * @param path a path of a node to check
     * @return a hash or empty array if node does not exist
     */
    public byte[] hashOf(String... path) {
        return hashOf(List.of(path));
    }

    /**
     * Returns a subtree with given node as a root.
     *
     * @param path a path of a node to be a subtree root
     * @return Some(subtree) if path exists, None otherwise
     */
    public Option<MerkleTree<V>> subtree(List<String> path) {
        return root.findNode(path).map(node -> new MerkleTree<>(valueSerializer, hashAlgorithm, node));
    }

    /**
     * Returns a subtree with given node as a root.
     *
     * @param path a path of a node to be a subtree root
     * @return Some(subtree) if path exists, None otherwise
     */
    public Option<MerkleTree<V>> subtree(String... path) {
        return subtree(List.of(path));
    }

    /**
     * Hash of a root node.
     *
     * @return a copy of root node's hash
     */
    public byte[] hash() {
        return root.hash().clone();
    }

    @Override
    public String toString() {
        return root.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MerkleTree<?> that = (MerkleTree<?>) o;
        return Objects.equals(root, that.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root);
    }
}

final class MTNode<V> {

    private final byte[] hash;
    private final Option<V> value;
    private final Map<String, MTNode<V>> children;
    private final HashAlgorithm hashAlgorithm;

    static <V> MTNode<V> empty(HashAlgorithm hashAlgorithm) {
        return new MTNode<>(hashAlgorithm, new byte[0], Option.none(), HashMap.empty());
    }

    static <V> MTNode<V> leaf(HashAlgorithm hashAlgorithm, byte[] hash, V value) {
        return new MTNode<>(hashAlgorithm, hash, Option.of(value), HashMap.empty());
    }

    private MTNode(
            HashAlgorithm hashAlgorithm,
            byte[] hash,
            Option<V> value,
            Map<String, MTNode<V>> children) {
        this.hashAlgorithm = hashAlgorithm;
        this.hash = hash.clone();
        this.value = value;
        this.children = children;
    }

    MTNode<V> addChild(final List<String> path, final MTNode<V> child) {
        if (path.isEmpty()) {
            return child;
        } else {
            String label = path.head();
            MTNode<V> newChild = children.get(label).fold(
                    () -> MTNode.<V>empty(hashAlgorithm).addChild(path.tail(), child),
                    node -> node.addChild(path.tail(), child)
            );
            return addChild(label, newChild);
        }
    }

    Option<V> value() {
        return value;
    }

    Option<MTNode<V>> findNode(List<String> path) {
        return path.headOption().fold(
                () -> Option.of(this),
                head -> children.get(head).flatMap(child -> child.findNode(path.tail()))
        );
    }

    byte[] hash() {
        return hash;
    }

    private MTNode<V> addChild(String label, MTNode<V> child) {
        final Map<String, MTNode<V>> newChildren = children.put(label, child);
        byte[] newHash = composeHashes(newChildren.iterator(this::hashForChild));
        return Arrays.equals(newHash, hash) ? this : new MTNode<>(
                hashAlgorithm,
                newHash,
                value,
                newChildren
        );
    }

    private byte[] hashForChild(String label, MTNode<V> child) {
        return composeHashes(List.of(label.getBytes(), child.hash()));
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
        MTNode<?> mtNode = (MTNode<?>) o;
        return Arrays.equals(hash, mtNode.hash);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(hash);
    }
}
