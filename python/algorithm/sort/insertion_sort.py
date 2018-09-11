#!/usr/bin/env python
# -*- coding: utf-8 -*-


import tools
from tools import exectime


@exectime
def insert_sort(array):
    for i in range(len(array)):
        for j in range(i):
            if array[i] < array[j]:
                array.insert(j, array.pop(i))
                break
    return array


def main():
    """
   ﻿（1）从第一个元素开始，该元素可以认为已经被排序；
   （2）取出下一个元素，在已经排序的元素序列中从后向前扫描；
   （3）如果该元素（已排序）大于新元素，将该元素移到下一位置；
   （4）重复步骤3，直到找到已排序的元素小于或者等于新元素的位置；
   （5）将新元素插入到该位置后；
   （6）重复步骤2~5。
    """

    # tools.dump_random_array()
    array = tools.load_random_array()
    print(insert_sort(array) == sorted(array))


if __name__ == '__main__':
    main()
