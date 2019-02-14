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
 *
 * Each node is labelled with a {@code String} label. A path of a node is defined as a list of labels from root to a
 * given node.
 *
 * @author <a href="mailto:piotr.jaszczyk@nokia.com">Piotr Jaszczyk</a>
 * @since 1.1.2
 */
public final class MerkleTree<V> {

    private final Function1<V, byte[]> hashProvider;
    private final MTNode<V> root;

    private MerkleTree(@NotNull Function1<V, byte[]> hashProvider, @NotNull MTNode<V> root) {
        this.hashProvider = Objects.requireNonNull(hashProvider);
        this.root = Objects.requireNonNull(root);
    }

    /**
     * Create an empty tree with given serializer and using default digest algorithm as a hash function.
     *
     * @param serializer a way of serializing a value to array of bytes
     * @param <V> type of values kept in a tree
     * @return empty tree
     */
    public static @NotNull <V> MerkleTree<V> emptyWithDefaultDigest(@NotNull Function1<V, byte[]> serializer) {
        return emptyWithDigest(MTNode.DEFAULT_DIGEST_ALGORITHM, serializer);
    }

    /**
     * Create an empty tree with given serializer and given digest algorithm used as a hash function.
     *
     * @param digestAlgorithmName name of a digest algorithm as used by {@link MessageDigest#getInstance(String)}
     * @param serializer a way of serializing a value to array of bytes
     * @param <V> type of values kept in a tree
     * @return empty tree
     */
    public static @NotNull <V> MerkleTree<V> emptyWithDigest(
            @NotNull String digestAlgorithmName,
            @NotNull Function1<V, byte[]> serializer) {
        return emptyWithHashProvider(value -> {
            try {
                MessageDigest digest = MessageDigest.getInstance(digestAlgorithmName);
                return digest.digest(serializer.apply(value));
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalArgumentException("Unsupported hash algorithm " + digestAlgorithmName, e);
            }
        });
    }

    /**
     * Create an empty tree with given hash function.
     *
     * @param hashProvider a function which calculates a hash of value
     * @param <V> type of values kept in a tree
     * @return empty tree
     */
    public static <V> MerkleTree<V> emptyWithHashProvider(Function1<V, byte[]> hashProvider) {
        return new MerkleTree<>(hashProvider, MTNode.empty());
    }

    /**
     * Assigns a value to a given path.
     *
     * Overrides current value if already exists.
     *
     * @param value a value to assign
     * @param path path of labels from root
     * @return an updated tree instance or <code>this</code> if hashes are the same
     */
    public MerkleTree<V> add(V value, String... path) {
        return add(List.of(path), value);
    }

    /**
     * Assigns a value to a given path.
     *
     * Overrides current value if already exists.
     *
     * @param path path of labels from root
     * @param value a value to assign
     * @return an updated tree instance or <code>this</code> if hashes are the same
     */
    public MerkleTree<V> add(List<String> path, V value) {
        final MTNode<V> result = root.addChild(path, MTNode.leaf(hashProvider.apply(value), value));
        return Arrays.equals(result.hash(), root.hash()) ? this : new MerkleTree<>(hashProvider, result);
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
     * @param path a path to a subtree to compare
     * @return true if hashes are the same, false otherwise
     */
    public boolean isSame(MerkleTree<V> other, String... path) {
        return isSame(List.of(path), other);
    }

    /**
     * Checks if nodes under given path are the same in {@code this} and {@code other} tree.
     *
     * @param other a tree to compare with
     * @param path a path to a subtree to compare
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
        return root.findNode(path).map(root1 -> new MerkleTree<V>(hashProvider, root1));
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

    static final String DEFAULT_DIGEST_ALGORITHM = "SHA-256";
    private final byte[] hash;
    private final Option<V> value;
    private final Map<String, MTNode<V>> children;

    static <V> MTNode<V> empty() {
        return new MTNode<>(new byte[0], Option.none(), HashMap.empty());
    }

    static <V> MTNode<V> leaf(byte[] hash, V value) {
        return new MTNode<>(hash, Option.of(value), HashMap.empty());
    }

    private MTNode(byte[] hash, Option<V> value, Map<String, MTNode<V>> children) {
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
                    () -> MTNode.<V>empty().addChild(path.tail(), child),
                    node -> node.addChild(path.tail(), child)
            );
            return addChild(label, newChild);
        }
    }

    private MTNode<V> addChild(String label, MTNode<V> child) {
        final Map<String, MTNode<V>> newChildren = children.put(label, child);
        byte[] newHash = composeHashes(newChildren.values().map(MTNode::hash));
        return Arrays.equals(newHash, hash) ? this : new MTNode<>(
                newHash,
                value,
                newChildren
        );
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

    public Option<V> value() {
        return value;
    }

    private static byte[] composeHashes(Iterable<byte[]> hashes) {
        try {
            MessageDigest digest = MessageDigest.getInstance(DEFAULT_DIGEST_ALGORITHM);
            for (byte[] it : hashes) {
                digest.update(it);
            }
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
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
