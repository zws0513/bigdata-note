#!/usr/bin/env python
# -*- coding: utf-8 -*-


import tools


@tools.exectime
def radix_sort(array):
    bucket, digit = [[]], 0
    while len(bucket[0]) != len(array):
        bucket = [[], [], [], [], [], [], [], [], [], []]
        for i in range(len(array)):
            num = (array[i] // 10 ** digit) % 10
            bucket[num].append(array[i])
        array.clear()
        for i in range(len(bucket)):
            array += bucket[i]
        digit += 1
    return array


def main():
    """﻿基数排序

    （1）取得数组中的最大数，并取得位数；
    （2）arr为原始数组，从最低位开始取每个位组成radix数组；
    （3）对radix进行计数排序（利用计数排序适用于小范围数的特点）

    :return:
    """

    array = tools.load_random_array()
    print radix_sort(array) == sorted(array)


if __name__ == '__main__':
    main()