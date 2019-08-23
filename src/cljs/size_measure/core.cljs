(ns size-measure.core
  (:require
   [reagent.core :as r :refer [atom]]
   [re-frame.core :as rf]
   [ajax.core :as http]
   ["antd" :as ant]))



(def FormItem (.-Item ant/Form))
(def InputSearch (.-Search ant/Input))

(def size-base-url "http://106.12.6.24:8080")
(def size-body-size-list (str size-base-url "/public/admin/measure/data/r"))
(def size-standard-size-list (str size-base-url "/public/standard/size/data/record"))
(def size-standard-size-save (str size-base-url "/public/standard/size/data/entry"))

#_(def body-size-list (rf/subscribe [:measuresize/body-size]))

(rf/reg-event-fx
 :search-body-size-list
 (fn [cofx [_ param]]
   {:http-xhrio {:uri size-body-size-list
                 :method :get
                 :params param
                 :timeout 10000
                 :format          (http/json-request-format)
                 :response-format (http/json-response-format {:keywords? true})
                 :on-success [::set-size-body-size-list]
                 :on-failure [:common/get-error]}}))

(defn size-search []
  (let [search (r/atom {:search-name ""})]
    (fn [] [:div {:style {:width "90%" :margin "auto" :margin-top 10 :margin-bottom 10}}
           [:div {:style {:text-align "center" :margin-bottom 10}}
            [:span "标准数据录入页面"]]
           [:> ant/Row
            [:> ant/Col {:span 6} "姓名:"]
            [:> ant/Col {:span 18}
             [:> InputSearch
              {:placeholder "请输入姓名" :enterButton "搜索"
               :value (:search-name @search)
               :on-change #(swap! search assoc :search-name (-> % .-target .-value))
               :onSearch (fn [] (rf/dispatch [:search-body-size-list {:measureNameLike (:search-name @search) :page 0 :size 1}])) }]]
            ]])))

(defn ^:dev/after-load mount-root []
  (r/render [size-search]
            (.getElementById js/document "app")))

(defn init! []
  (mount-root))
