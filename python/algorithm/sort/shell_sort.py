#!/usr/bin/env python
# -*- coding: utf-8 -*-


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

    print "插入排序"


if __name__ == '__main__':
    main()
