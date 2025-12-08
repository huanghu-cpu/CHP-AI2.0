package com.huzi.chpai.Algorithm;

import com.huzi.chpai.entity.ACNode;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class ACAutomaton {
    // 违禁词列表
    private static final Set<String> SENSITIVE_WORDS = Set.of(
            "违禁词1", "违禁词2", "敏感词", "非法内容", "不良信息","虎子"
    );

    // AC自动机根节点
    private static ACNode root = new ACNode();

    // 静态初始化块，构建AC自动机
    static {
        buildACAutomaton();
    }

    /**
     * 构建AC自动机
     */
    private static void buildACAutomaton() {
        // 1. 构建Trie树
        for (String word : SENSITIVE_WORDS) {
            ACNode current = root;
            for (char c : word.toCharArray()) {
                current.children.putIfAbsent(c, new ACNode());
                current = current.children.get(c);
            }
            current.isEnd = true;
            current.length = word.length();
        }

        // 2. 构建失败指针
        buildFailPointer();
    }

    /**
     * 构建失败指针（BFS遍历）
     */
    private static void buildFailPointer() {
        Queue<ACNode> queue = new LinkedList<>();

        // 根节点的子节点的失败指针指向根节点
        for (ACNode child : root.children.values()) {
            child.fail = root;
            queue.offer(child);
        }

        while (!queue.isEmpty()) {
            ACNode current = queue.poll();

            for (Map.Entry<Character, ACNode> entry : current.children.entrySet()) {
                char c = entry.getKey();
                ACNode child = entry.getValue();
                ACNode failNode = current.fail;

                // 寻找失败指针
                while (failNode != null && !failNode.children.containsKey(c)) {
                    failNode = failNode.fail;
                }

                if (failNode == null) {
                    child.fail = root;
                } else {
                    child.fail = failNode.children.get(c);
                }

                queue.offer(child);
            }
        }
    }

    /**
     * 基于AC自动机检查文本是否包含违禁词
     * @param text 待检查的文本
     * @return 如果包含违禁词则返回true，否则返回false
     */
    public static Boolean isSensitive(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        ACNode current = root;
        char[] chars = text.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            // 如果当前节点没有对应字符的子节点，则跳转到失败指针
            while (current != root && !current.children.containsKey(c)) {
                current = current.fail;
            }

            // 如果当前节点有对应字符的子节点，则移动到子节点
            if (current.children.containsKey(c)) {
                current = current.children.get(c);
            } else {
                current = root;
            }

            // 检查当前节点是否是模式串的结尾
            ACNode temp = current;
            while (temp != root) {
                if (temp.isEnd) {
                    return true;
                }
                temp = temp.fail;
            }
        }

        return false;
    }
}
