package com.huzi.chpai.entity;

import java.util.HashMap;
import java.util.Map;

// AC自动机节点类
public  class ACNode {
    public Map<Character, ACNode> children = new HashMap<>();
    public ACNode fail;
    public boolean isEnd = false;
    public int length = 0; // 记录模式串长度
}
