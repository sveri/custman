(ns de.sveri.custman.service.questionnaire)

(def ross-questions
  {:types {:type-1 "Befinden Sie sich unter einer Dunklen Wolke?"
           :type-2 "Fühlen Sie sich \"bla\"?"}
   :questions
          [{:type :type-1 :points 3 :name "1.1"
            :text "Neigen Sie dazu, negativ zu denken, das Glas halb leer statt halb voll zu sehen?
                               Haben Sie düstere, pessimistische Gedanken?"}
           {:type :type-1 :points 3 :name "1.2" :text "Sind Sie häufig besorgt oder verängstigt?"}
           {:type :type-2 :points 3 :name "2.1"
            :text "Fühlen Sie sich häufig niedergeschlagen, auf eine flaue, gelangweilte, apathische Art und Weise - die hier kurz als \"Bla\"-Depression bezeichnet werden soll?"}]})
