#!/usr/bin/env python
# -*- coding: utf-8 -*-


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

    def recursive(array):
        if len(array) == 1:
            return array
        mid = len(array) // 2
        arr_l = recursive(array[:mid])
        arr_r = recursive(array[mid:])
        return merge_arr(arr_l, arr_r)

    return recursive(array)


def main():

    print "插入排序"


if __name__ == '__main__':
    main()