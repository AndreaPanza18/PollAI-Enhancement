import joblib
import numpy as np

class uova_model:
    def __init__(self):
        self.model = joblib.load('uova_model.pkl')

    def _preprocess_data(self, df):

        mangime_dict = {"Type A": 1, "Type B": 2, "Type C": 3}
        df["mangime"] = df["mangime"].map(mangime_dict)

        razza_dict = {"Leghorn": 1, "Rhode Island Red": 0.5, "Sussex": 0.7, "Plymouth Rock": 0}
        df["razza"] = df["razza"].map(razza_dict)

        return df

    def predict(self, df):
        df = self._preprocess_data(df)
        pred = self.model.predict(df)
        return np.sum(pred)

