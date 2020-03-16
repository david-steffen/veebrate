module Main exposing (main)

import Html as H exposing (Html)
import Html.Attributes as A
import Html.Events as E
import Json.Decode as JD
import Debug


-- Update
-- ------


type alias Model =
    { readyTimes : Bool
    , readyProjects : Bool
    , error : Maybe String
    }


type Msg
    = OnChange


init : Model -> ( Model, Cmd Msg )
init =
    { readyTimes = False
    , readyProjects = False
    , error = Nothing
    }


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        OnChange ->
            Debug.crash "here"



subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none



-- View
-- ----


notFound : Html Msg
notFound =
    H.h1 [] [ H.text "Page not found" ]


home : Html Msg
home =
    H.div []
        [ H.h1 [] [ H.text "Home Page" ]
        , H.p [] [ H.text "moo" ]
        ]


about : Html Msg
about =
    H.h1 [] [ H.text "About" ]

errorMsg : Maybe String -> String
errorMsg error =
    case error of
        Just _ ->
            toString error
        Nothing ->
          "moo"

loading : Maybe String -> Html Msg
loading error =
    H.div []
        [ H.h1 [] [ H.text "Loading..." ]
        , H.p [] [ H.text (toString error) ]
        ]



view : Model -> Html Msg
view model =
    H.div []
        [ nav
        , content model
        ]



link : Sitemap -> String -> Html Msg
link route label =
    let
        opts =
            { preventDefault = True, stopPropagation = True }
    in
        H.a
            [ A.href (Routes.toString route)
            , E.onWithOptions "click" opts (JD.succeed <| RouteTo route)
            ]
            [ H.text label ]



-- Main
-- ----


main : Program Never Model Msg
main =
    Html.program
        { init = init
        , update = update
        , view = view
        , subscriptions = subscriptions
        }
