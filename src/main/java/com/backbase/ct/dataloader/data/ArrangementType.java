package com.backbase.ct.dataloader.data;

public enum ArrangementType {

    GENERAL {
        @Override
        public String toString() {
            return "General";
        }
    },
    INTERNATIONAL_TRADE {
        @Override
        public String toString() {
            return "International Trade";
        }
    },
    FINANCE_INTERNATIONAL {
        @Override
        public String toString() {
            return "Finance International";
        }
    },
    PAYROLL {
        @Override
        public String toString() {
            return "Payroll";
        }
    }
}
