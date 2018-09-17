#!/usr/bin/env python
# -*- coding: utf-8 -*-


import tools


@tools.exectime
def shell_sort(array):
    gap = len(array)
    while gap > 1:
        gap = gap // 2
        for i in range(gap, len(array)):
            for j in range(i % gap, i, gap):
                if array[i] < array[j]:
                    array[i], array[j] = array[j], array[i]
    return array


def main():
    """希尔排序

    （1）选择一个增量序列t1，t2，…，tk，其中ti>tj，tk=1；
    （2）按增量序列个数k，对序列进行k 趟排序；
    （3）每趟排序，根据对应的增量ti，将待排序列分割成若干长度为m 的子序列，分别对各子表进行直接插入排序。
        仅增量因子为1 时，整个序列作为一个表来处理，表长度即为整个序列的长度。
    :return:
    """

    array = tools.load_random_array()
    print shell_sort(array) == sorted(array)


if __name__ == '__main__':
    main()
