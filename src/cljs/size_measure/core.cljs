(ns size-measure.core
  (:require
   [reagent.core :as r :refer [atom]]
   [re-frame.core :as rf]
   [ajax.core :as http]
   [day8.re-frame.http-fx]
   [size-measure.utils :as utils]
   ["antd" :as ant]))

(def FormItem (.-Item ant/Form))
(def InputSearch (.-Search ant/Input))

(def size-base-url "http://192.168.0.189:8080/automeasure")
(def size-body-size-list (str size-base-url "/public/admin/measure/data/r"))
(def size-standard-size-list (str size-base-url "/public/standard/size/data/record"))
(def size-standard-size-save (str size-base-url "/public/standard/size/data/entry"))

(declare data->db)

(rf/reg-sub
 :body-size
 (fn [db _]
   (prn db)
   (-> db
       :measuresize
       :body-size)))


(def info (rf/subscribe [:body-size]))

(rf/reg-event-db
 :init
 (fn [_ _]
   {:measuresize
    {:body-size nil}}))

(rf/dispatch-sync [:init])

(defn- concat-key [keys]
  (concat [:measuresize] keys))

(defn- data->db [db keys value]
  (assoc-in db (concat-key keys) value))

(defn- get-value-from-db [db keys]
  (get-in db (concat-key keys)))

;;存储量体数据列表内容
(rf/reg-event-db
 ::set-size-body-size-list
 (fn [db [_ list]]
   (let []
     (data->db db [:body-size] (-> list
                                   :data
                                   :content
                                   first)))))

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
               :onSearch (fn [] (rf/dispatch [:search-body-size-list
                                             {:measureNameLike (:search-name @search)
                                              :clientId "hl190806354513392989"
                                              :page 0 :size 1}])) }]]
            ]])))

(defn size-form []
  (let [form-data (r/atom {:neck 0 :shoulder 0})]

    (fn []
      (if (nil? @info)
        [:div "请先搜索"]
        (let [this (utils/get-form)]
          [:div
           [:> ant/Row
            [:> ant/Col {:span 12}
             [:div [:span "用户："] [:span (:userName @info)]]
             [:div [:span "身高："] [:span (:height @info)] [:span " cm"]]
             [:div [:span "体重："] [:span (:height @info)] [:span " kg"]]
             [:div [:span "性别："] [:span (if (= "f" (:sex @info)) "女" "男")]]]
            [:> ant/Col {:span 12}
             [:img {:src (:imageName @info)}]]]
           [:> ant/Form {:onSubmit (fn [] (rf/dispatch [:submit-standard-size @form-data]))}
            [:> FormItem {:label "颈围"}
             [:> ant/Row
              [:> ant/Col {:span 12}
               [:span (:neck @info)]]
              [:> ant/Col {:span 12}
               (utils/decorate-field this "neck"
                                     [:> ant/Input
                                      {:placeholder "输入手量颈围"
                                       :value (:neck @form-data)
                                       :on-change #(swap! form-data assoc :neck (-> % .-target .-value))}])]]]
            [:> FormItem {:label "肩宽"}
             [:> ant/Row
              [:> ant/Col {:span 12}
               [:span (:shoulder @info)]]
              [:> ant/Col {:span 12}
               (utils/decorate-field this "shoulder"
                                     [:> ant/Input
                                      {:placeholder "输入手量肩宽"
                                       :value (:shoulder @form-data)
                                       :on-change #(swap! form-data assoc :shoulder (-> % .-target .-value))}])]]]
            ]])))))



(defn ^:dev/after-load mount-root []
  (r/render [:div
             [size-search]
             (utils/create-form size-form)]
            (.getElementById js/document "app")))

(defn init! []
  (mount-root))
