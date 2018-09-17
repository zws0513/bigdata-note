#!/usr/bin/env python
# -*- coding: utf-8 -*-


import tools


@tools.exectime
def merge_sort(array):
    def merge_arr(arr_l, arr_r):
        arr = []
        while len(arr_l) and len(arr_r):
            if arr_l[0] <= arr_r[0]:
                arr.append(arr_l.pop(0))
            elif arr_l[0] > arr_r[0]:
                arr.append(arr_r.pop(0))

        if len(arr_l) != 0:
            arr += arr_l
        elif len(arr_r) != 0:
            arr += arr_r

        return arr

    def recursive(inner_array):
        if len(inner_array) == 1:
            return inner_array
        mid = len(inner_array) // 2
        arr_l = recursive(inner_array[:mid])
        arr_r = recursive(inner_array[mid:])
        return merge_arr(arr_l, arr_r)

    return recursive(array)


def main():
    """归并排序

    （1）把长度为n的输入序列分成两个长度为n/2的子序列；
    （2）对这两个子序列分别采用归并排序；
    （3）将两个排序好的子序列合并成一个最终的排序序列。

    :return:
    """

    array = tools.load_random_array()
    print merge_sort(array) == sorted(array)


if __name__ == '__main__':
    main()
