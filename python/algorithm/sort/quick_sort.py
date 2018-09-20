#!/usr/bin/env python
# -*- coding: utf-8 -*-


import tools


@tools.exectime
def quick_sort(array):
    def recursive(begin, end):
        if begin >= end:
            return
        l, r = begin, end
        pivot = array[l]
        while l < r:
            while l < r and array[r] > pivot:
                r -= 1
            while l < r and array[l] <= pivot:
                l += 1
            array[l], array[r] = array[r], array[l]

        # array[begin]存的是pivot，已经无用，而当前array[l]的位置就是pivot应该在的位置，把array[l]的值移到pivot的左边
        array[l], array[begin] = pivot, array[l]
        recursive(begin, l - 1)
        recursive(r + 1, end)

    recursive(0, len(array) - 1)
    return array


@tools.exectime
def quick_sort2(array):
    def recursive2(begin, end):
        if begin >= end:
            return

        l, r = begin, end
        key = array[l]

        # 循环判断直到遍历全部
        while l < r:
            # 从右边开始查找大于参考点的值
            while l < r and array[r] >= key:
                r -= 1
            array[l] = array[r]  # 这个位置的值先挪到左边

            # 从左边开始查找小于参考点的值
            while l < r and array[l] <= key:
                l += 1
            array[r] = array[l]  # 这个位置的值挪到右边

        # 写回改成的值
        array[l] = key

        # 递归，并返回结果
        recursive2(begin, l - 1)    # 递归左边部分
        recursive2(l + 1, end)   # 递归右边部分
    recursive2(0, len(array) - 1)
    return array


def main():
    """﻿快速排序

    （1）从数列中挑出一个元素，称为 “基准”（pivot）；
    （2）重新排序数列，所有元素比基准值小的摆放在基准前面，所有元素比基准值大的摆在基准的后面（相同的数可以到任一边）。
        在这个分区退出之后，该基准就处于数列的中间位置。这个称为分区（partition）操作；
    （3）递归地（recursive）把小于基准值元素的子数列和大于基准值元素的子数列排序。

    :return:
    """

    array = tools.load_random_array()
    # print quick_sort(array) == sorted(array)
    print quick_sort2(array) == sorted(array)


if __name__ == '__main__':
    main()