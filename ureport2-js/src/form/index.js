/**
 * Created by Jacky.Gao on 2017-10-24.
 */
import FormBuilder from './FormBuilder.js';

$(document).ready(function(){
    (function($){
        $.fn.datetimepicker.dates['zh-CN'] = {
            days: ["������", "����һ", "���ڶ�", "������", "������", "������", "������", "������"],
            daysShort: ["����", "��һ", "�ܶ�", "����", "����", "����", "����", "����"],
            daysMin:  ["��", "һ", "��", "��", "��", "��", "��", "��"],
            months: ["һ��", "����", "����", "����", "����", "����", "����", "����", "����", "ʮ��", "ʮһ��", "ʮ����"],
            monthsShort: ["һ��", "����", "����", "����", "����", "����", "����", "����", "����", "ʮ��", "ʮһ��", "ʮ����"],
            today: "����",
            suffix: [],
            meridiem: ["����", "����"]
        };
    }(jQuery));
    const formBuilder=new FormBuilder($("#container"));
    formBuilder.initData(window.parent.__current_report_def);
});