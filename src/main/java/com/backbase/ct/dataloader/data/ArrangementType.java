package com.backbase.ct.dataloader.data;

public enum ArrangementType {

    GENERAL_RETAIL {
        @Override
        public String toString() {
            return "General Retail";
        }
    },
    GENERAL_BUSINESS {
        @Override
        public String toString() {
            return "General Business";
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
