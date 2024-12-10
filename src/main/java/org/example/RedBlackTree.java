package org.example;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Узел может быть либо красным, либо чёрным и имеет двух потомков;
 * Корень — как правило чёрный. Это правило слабо влияет на работоспособность модели, так как цвет корня всегда можно изменить с чёрного на красный;
 * Все листья, не содержащие данных — чёрные.
 * Оба потомка каждого красного узла — чёрные.
 * Любой простой путь от узла-предка до листового узла-потомка содержит одинаковое число чёрных узлов.
 * <a href="https://www.happycoders.eu/algorithms/red-black-tree-java/">https://www.happycoders.eu/algorithms/red-black-tree-java/</a>
 *
 * @param <K>
 */
public class RedBlackTree<K extends Comparable<K>> {

    private Node<K> root;

    private TreePrinter<Node<K>> printer = new TreePrinter<>(Node::toString, Node::getLeft, Node::getRight);

    {
        printer.setSquareBranches(true);
        printer.setHspace(1);
        printer.setHspace(1);
    }

    public boolean add(K key) {

        Node<K> parent = null;
        Node<K> current = root;

        while (current != null) {
            parent = current;
            if (key.compareTo(current.key) == 0) {
                return false;
            }
            if (key.compareTo(current.key) < 0) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
        current = new Node<>(parent, key);
        if (parent == null) {
            root = current;
        } else if (key.compareTo(parent.key) < 0) {
            parent.setLeft(current);
        } else {
            parent.setRight(current);
        }

        fixAfterInsertion(current);


        return true;
    }

    private void fixAfterInsertion(Node<K> node) {
        // Case 1: Parent is null, we've reached the root, the end of the recursion
        Node<K> parent = node.parent;
        if (parent == null) {
            node.isRed = false;
            return;
        }
        if (!parent.isRed) {
            // Parent is black --> nothing to do
            return;
        }
        // From here on, parent is red
        Node<K> grandpa = parent.parent;

        // Case 2:
        // Not having a grandparent means that parent is the root. If we enforce black roots
        // (rule 2), grandparent will never be null, and the following if-then block can be
        // removed.
        if (grandpa == null) {
            parent.isRed = false;
            return;
        }

        // Get the uncle (may be null/nil, in which case its color is BLACK)
        Node<K> uncle = getUncle(parent);

        // Case 3: Uncle is red -> recolor parent, grandparent and uncle
        if (uncle != null && uncle.isRed) {
            parent.isRed = false;
            grandpa.isRed = true;
            uncle.isRed = false;

            fixAfterInsertion(grandpa);
        } else if (parent == grandpa.left) {
            // Parent is left child of grandparent

            // Case 4a: Uncle is black and node is left->right "inner child" of its grandparent
            if (node == parent.right) {
                leftRotate(parent);
                // Let "parent" point to the new root node of the rotated sub-tree.
                // It will be recolored in the next step, which we're going to fall-through to.
                parent = node;
            }

            // Case 5a: Uncle is black and node is left->left "outer child" of its grandparent
            rightRotate(grandpa);

            // Recolor original parent and grandparent
            parent.isRed = false;
            grandpa.isRed = true;
        } else {
            // Parent is right child of grandparent

            // Case 4b: Uncle is black and node is right->left "inner child" of its grandparent
            if (node == parent.left) {
                rightRotate(parent);

                // Let "parent" point to the new root node of the rotated sub-tree.
                // It will be recolored in the next step, which we're going to fall-through to.
                parent = node;
            }

            // Case 5b: Uncle is black and node is right->right "outer child" of its grandparent
            leftRotate(grandpa);

            // Recolor original parent and grandparent
            parent.isRed = false;
            grandpa.isRed = true;
        }
    }

    private Node<K> getUncle(Node<K> parent) {
        Node<K> grandpa = parent.parent;
        if (grandpa.left == parent) {
            return grandpa.right;
        } else if (grandpa.right == parent) {
            return grandpa.left;
        }
        throw new IllegalStateException("Bad parent");
    }

    public boolean contains(K key) {
        Node<K> current = root;
        while (current != null) {
            if (key.compareTo(current.key) == 0) {
                return true;
            }
            if (key.compareTo(current.key) < 0) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
        return false;
    }

    public boolean remove(K key) {
        Node<K> toDel = root;
        while (toDel != null) {
            if (key.compareTo(toDel.key) == 0) {
                break;
            }
            if (key.compareTo(toDel.key) < 0) {
                toDel = toDel.left;
            } else {
                toDel = toDel.right;
            }
        }
        if (toDel == null) {
            return false;
        }
        Node<K> movedUpNode;
        boolean isDeletedRed;
        if (toDel.left == null || toDel.right == null) {
            movedUpNode = deleteWithZeroOrOneChild(toDel);
            isDeletedRed = toDel.isRed;
        } else {
            Node<K> inOrderSuccesor = findMinimum(toDel.right);
            toDel.setKey(inOrderSuccesor.key);
            movedUpNode = deleteWithZeroOrOneChild(inOrderSuccesor);
            isDeletedRed = inOrderSuccesor.isRed;
        }

        if (isDeletedRed) {
            fixAfterDeletion(movedUpNode);
        }

        if (movedUpNode instanceof NilNode) {
            replace(movedUpNode.parent, movedUpNode, null);
        }
        return true;
    }

    public void clear() {
        root = null;
    }

    private void fixAfterDeletion(Node<K> node) {
        if (node == null) {
            return;
        }
        // Case 1: Examined node is root, end of recursion
        if (node == root) {
            node.isRed = false;
            return;
        }
        Node<K> sibling = getSibling(node);

        // Case 2: Red sibling
        if (sibling.isRed) {
            handleRedSibling(node, sibling);
            sibling = getSibling(node);
        }

        // Cases 3+4: Black sibling with two black children
        if (isBlack(sibling.left) && isBlack(sibling.right)) {
            sibling.isRed = true;

            if (node.parent.isRed) {
                // Case 3: Black sibling with two black children + red parent
                node.parent.isRed = false;
            } else {
                // Case 4: Black sibling with two black children + black parent
                fixAfterDeletion(node.parent);
            }
        } else {
            // Case 5+6: Black sibling with at least one red child
            handleBlackSiblingWithAtLeastOneRedChild(node, sibling);
        }

    }

    private void handleBlackSiblingWithAtLeastOneRedChild(Node<K> node, Node<K> sibling) {
        boolean isLeftChild = node == node.parent.left;
        // Case 5: Black sibling with at least one red child + "outer nephew" is black
        // --> Recolor sibling and its child, and rotate around sibling
        if (isLeftChild && isBlack(sibling.right)) {
            if (sibling.left != null) {
                sibling.left.isRed = false;
            }
            sibling.isRed = true;
            rightRotate(sibling);
            sibling = node.parent.right;
        } else if (!isLeftChild && isBlack(sibling.left)) {
            if (sibling.right != null) {
                sibling.right.isRed = false;
            }
            sibling.isRed = true;
            leftRotate(sibling);
            sibling = node.parent.left;
        }

        // Case 6: Black sibling with at least one red child + "outer nephew" is red
        // --> Recolor sibling + parent + sibling's child, and rotate around parent
        sibling.isRed = node.parent.isRed;
        node.parent.isRed = false;
        if (isLeftChild) {
            sibling.right.isRed = false;
            leftRotate(node.parent);
        } else {
            sibling.left.isRed = false;
            rightRotate(node.parent);
        }
    }

    private void handleRedSibling(Node<K> node, Node<K> sibling) {
        sibling.isRed = false;
        node.parent.isRed = true;

        if (node == node.parent.left) {
            rightRotate(node.parent);
        } else {
            leftRotate(node.parent);
        }
    }

    private boolean isBlack(Node<K> node) {
        return node == null || !node.isRed;
    }

    private Node<K> getSibling(Node<K> node) {
        Node<K> parent = node.parent;
        if (node == parent.left) {
            return parent.right;
        } else if (node == parent.right) {
            return parent.left;
        }
        throw new IllegalStateException("Bad parent");
    }

    private Node<K> findMinimum(Node<K> node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    private Node<K> deleteWithZeroOrOneChild(Node<K> node) {
        if (node.left != null) {
            replace(node.parent, node, node.left);
            return node.left;
        } else if (node.right != null) {
            replace(node.parent, node, node.right);
            return node.right;
        }
        Node<K> newChild = !node.isRed ? new NilNode<>() : null;
        replace(node.parent, node, newChild);

        return newChild;
    }

    private void replace(Node<K> node, Node<K> old, Node<K> update) {
        if (node == null) {
            root = update;
        } else if (node.left == old) {
            node.setLeft(update);
        } else if (node.right == old) {
            node.setRight(update);
        } else {
            throw new RuntimeException("Невозможно заменить узел");
        }
        if (update != null) {
            update.parent = node;
        }
    }


    private void leftRotate(Node<K> node) {
        Node<K> right = node.right;
        node.right = right.left;
        if (right.left != null) {
            right.left.parent = node;
        }
        right.parent = node.parent;
        if (node.parent == null) {
            root = right;
        } else if (node == node.parent.left) {
            node.parent.left = right;
        } else {
            node.parent.right = right;
        }
        right.left = node;
        node.parent = right;
    }

    private void rightRotate(Node<K> node) {
        Node<K> left = node.left;
        node.left = left.right;
        if (left.right != null) {
            left.right.parent = node;
        }
        left.parent = node.parent;
        if (node.parent == null) {
            root = left;
        } else if (node == node.parent.right) {
            node.parent.right = left;
        } else {
            node.parent.left = left;
        }
        left.right = node;
        node.parent = left;
    }

    public List<K> leftCurRight() {
        List<K> result = new ArrayList<>();
        leftCurRight(root, result);
        return result;
    }

    private void leftCurRight(Node<K> curr, List<K> result) {
        if (curr == null) {
            return;
        }
        leftCurRight(curr.left, result);
        result.add(curr.key);
        leftCurRight(curr.right, result);
    }

    public void print() {
        printer.printTree(root);
    }

    // Метод для задачи про рыцарей

    // Получаем ноду по ключу
    private Node<K> getByKey(K key) {
        Node<K> tmp = root;
        while (tmp != null) {
            if (key.compareTo(tmp.key) == 0) {
                return tmp;
            }
            if (key.compareTo(tmp.key) < 0) {
                tmp = tmp.left;
            } else {
                tmp = tmp.right;
            }
        }
        return tmp != null ? tmp : null;
    }

    // Получим значение K по ключу
    public K getByKeyForWinner(K key) {
        Node<K> node = getByKey(key);
        return node != null ? node.k : null;
    }

    public void setK(K win, K[] all) {
        for (K tmp : all) {
            if (tmp != win) {
                Node<K> lost = getByKey(tmp);
                if (lost != null && lost.k == null) {
                    lost.k = win;
                }
            }
        }
    }


    @Getter
    @Setter
    static class Node<K extends Comparable<K>> {
        K key;
        boolean isRed;
        Node<K> parent;
        Node<K> left;
        Node<K> right;
        K k = null;

        Node(Node<K> parent, K key) {
            this.parent = parent;
            this.key = key;
            this.isRed = parent != null;
        }

        public void setLeft(Node<K> left) {
            this.left = left;
        }

        public void setRight(Node<K> right) {
            this.right = right;
        }


        public String toString() {
            return String.format("%s [%s]", key, isRed ? "red" : "black");
        }

    }

    static class NilNode<K extends Comparable<K>> extends Node<K> {

        NilNode() {
            super(null, null);
            this.isRed = false;
        }
    }
}
