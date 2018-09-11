#!/usr/bin/env python
# -*- coding: utf-8 -*-


def select_sort(array):
    for i in range(len(array)):
        x = i  # min index
        for j in range(i, len(array)):
            if array[j] < array[x]:
                x = j
        array[i], array[x] = array[x], array[i]
    return array


def main():

    print "插入排序"


if __name__ == '__main__':
    main()
