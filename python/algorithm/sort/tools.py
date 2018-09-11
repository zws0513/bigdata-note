#!/usr/bin/env python
# -*- coding: utf-8 -*-


from random import random
from json import dumps, loads
from datetime import datetime


# 生成随机数文件
def dump_random_array(file='numbers.json', size=10 ** 4):
    fo = open(file, 'w', 1024)
    numlst = list()
    for i in range(size):
        numlst.append(int(random() * 10 ** 10))
    fo.write(dumps(numlst))
    fo.close()


# 加载随机数列表
def load_random_array(file='numbers.json'):
    fo = open(file, 'r', 1024)
    try:
        numlst = fo.read()
    finally:
        fo.close()
    return loads(numlst)


# 显示函数执行时间
def exectime(func):
    def inner(*args, **kwargs):
        begin = datetime.now()
        result = func(*args, **kwargs)
        end = datetime.now()
        inter = end - begin
        print('E-time:{0}.{1}'.format(
            inter.seconds,
            inter.microseconds
        ))
        return result

    return inner