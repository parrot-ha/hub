<template>
  <v-container fluid>
    <v-layout>
      <v-row>
        <v-col>
          <v-card>
            <v-card-title
              >Extensions
              <v-tooltip bottom
                ><template v-slot:activator="{ on }">
                  <v-btn
                    class="ma-2"
                    text
                    icon
                    color="blue lighten-2"
                    @click="loadExtensions(true)"
                    v-on="on"
                  >
                    <v-icon>mdi-refresh</v-icon>
                  </v-btn>
                </template>
                <span>Refresh Extension List</span>
              </v-tooltip>
            </v-card-title>
            <v-card-text>
              <v-tabs v-model="tab">
                <v-tab>Installed</v-tab>
                <v-tab>Available</v-tab>
                <v-tab>Locations</v-tab>
              </v-tabs>
              <v-tabs-items v-model="tab">
                <v-tab-item>
                  <v-card>
                    <v-card-text
                      >Installed Extensions
                      <v-simple-table>
                        <thead>
                          <tr>
                            <th scope="col" style="width:5%">Actions</th>
                            <th scope="col" style="width:20%">Name</th>
                            <th scope="col" style="width:75%">Description</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr
                            v-for="extension in installedExtensions"
                            :key="extension.id"
                          >
                            <td>
                              <div>
                                <v-tooltip
                                  bottom
                                  v-if="extension.updateAvailable"
                                  ><template v-slot:activator="{ on }">
                                    <v-btn
                                      class="ma-2"
                                      text
                                      icon
                                      color="blue lighten-2"
                                      @click="updateExtension(extension.id)"
                                      v-on="on"
                                    >
                                      <v-icon
                                        >mdi-cloud-download-outline</v-icon
                                      >
                                    </v-btn>
                                  </template>
                                  <span>Update</span>
                                </v-tooltip>

                                <v-dialog v-model="deleteDialog" width="500">
                                  <template v-slot:activator="{ on, attrs }">
                                    <v-btn
                                      class="ma-2"
                                      text
                                      icon
                                      color="blue lighten-2"
                                      v-bind="attrs"
                                      v-on="on"
                                    >
                                      <v-icon>mdi-delete-outline</v-icon>
                                    </v-btn>
                                  </template>
                                  <v-card>
                                    <v-card-title
                                      class="text-h5 grey lighten-2"
                                    >
                                      Delete Extension
                                    </v-card-title>

                                    <v-card-text>
                                      Are you sure you want to delete this
                                      extension?
                                    </v-card-text>

                                    <v-divider></v-divider>

                                    <v-card-actions>
                                      <v-spacer></v-spacer>
                                      <v-btn
                                        color="primary"
                                        text
                                        @click="deleteExtension(extension.id)"
                                      >
                                        Delete
                                      </v-btn>
                                      <v-btn
                                        color="primary"
                                        text
                                        @click="deleteDialog = false"
                                      >
                                        Cancel
                                      </v-btn>
                                    </v-card-actions>
                                  </v-card>
                                </v-dialog>
                              </div>
                            </td>
                            <td>
                              <router-link
                                :to="{
                                  name: 'Extension',
                                  params: { id: extension.id }
                                }"
                                >{{ extension.name }}</router-link
                              >
                            </td>
                            <td>{{ extension.description }}</td>
                          </tr>
                        </tbody>
                      </v-simple-table>
                    </v-card-text>
                  </v-card>
                </v-tab-item>
                <v-tab-item>
                  <v-card>
                    <v-card-text>
                      <v-simple-table>
                        <thead>
                          <tr>
                            <th scope="col" style="width:5%">Actions</th>
                            <th scope="col" style="width:20%">Name</th>
                            <th scope="col" style="width:75%">Description</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr
                            v-for="extension in availableExtensions"
                            :key="extension.id"
                          >
                            <td>
                              <v-btn
                                class="ma-2"
                                text
                                icon
                                color="blue lighten-2"
                                @click="downloadExtension(extension.id)"
                              >
                                <v-icon>mdi-cloud-download-outline</v-icon>
                              </v-btn>
                            </td>
                            <td>{{ extension.name }}</td>
                            <td>{{ extension.description }}</td>
                          </tr>
                        </tbody>
                      </v-simple-table>
                    </v-card-text>
                  </v-card>
                </v-tab-item>
                <v-tab-item>
                  <v-card>
                    <v-card-text>
                      <v-data-table
                        :headers="headers"
                        :items="locations"
                        class="elevation-1"
                      >
                        <template v-slot:top>
                          <v-toolbar flat>
                            <v-spacer></v-spacer>
                            <v-dialog
                              v-model="locationsDialog"
                              max-width="500px"
                            >
                              <template v-slot:activator="{ on, attrs }">
                                <v-btn
                                  color="primary"
                                  dark
                                  class="mb-3"
                                  v-bind="attrs"
                                  v-on="on"
                                >
                                  New Item
                                </v-btn>
                              </template>
                              <v-card>
                                <v-card-title>
                                  <span class="text-h5">{{
                                    locationsFormTitle
                                  }}</span>
                                </v-card-title>

                                <v-card-text>
                                  <v-container>
                                    <v-row>
                                      <v-col cols="12">
                                        <v-text-field
                                          v-model="locationsEditedItem.name"
                                          label="Extension name"
                                        ></v-text-field>
                                      </v-col>
                                    </v-row>
                                    <v-row>
                                      <v-col cols="12">
                                        <v-select
                                          v-model="locationsEditedItem.type"
                                          :items="locationsTypes"
                                          label="Type"
                                        ></v-select>
                                      </v-col>
                                    </v-row>
                                    <v-row>
                                      <v-col cols="12">
                                        <v-text-field
                                          v-model="locationsEditedItem.location"
                                          label="Location"
                                        ></v-text-field>
                                      </v-col>
                                    </v-row>
                                  </v-container>
                                </v-card-text>

                                <v-card-actions>
                                  <v-spacer></v-spacer>
                                  <v-btn
                                    color="blue darken-1"
                                    text
                                    @click="locationsClose"
                                  >
                                    Cancel
                                  </v-btn>
                                  <v-btn
                                    color="blue darken-1"
                                    text
                                    @click="locationsSave"
                                  >
                                    Save
                                  </v-btn>
                                </v-card-actions>
                              </v-card>
                            </v-dialog>
                            <v-dialog
                              v-model="locationsDialogDelete"
                              max-width="500px"
                            >
                              <v-card>
                                <v-card-title class="text-h5"
                                  >Are you sure you want to delete this
                                  item?</v-card-title
                                >
                                <v-card-actions>
                                  <v-spacer></v-spacer>
                                  <v-btn
                                    color="blue darken-1"
                                    text
                                    @click="locationsCloseDelete"
                                    >Cancel</v-btn
                                  >
                                  <v-btn
                                    color="blue darken-1"
                                    text
                                    @click="locationsDeleteItemConfirm"
                                    >OK</v-btn
                                  >
                                  <v-spacer></v-spacer>
                                </v-card-actions>
                              </v-card>
                            </v-dialog>
                          </v-toolbar>
                        </template>
                        <template v-slot:item.actions="{ item }">
                          <v-icon
                            small
                            class="mr-2"
                            @click="locationsEditItem(item)"
                          >
                            mdi-pencil
                          </v-icon>
                          <v-icon small @click="locationsDeleteItem(item)">
                            mdi-delete
                          </v-icon>
                        </template>
                      </v-data-table>
                    </v-card-text>
                  </v-card>
                </v-tab-item>
              </v-tabs-items>
            </v-card-text>
            <v-card-actions> </v-card-actions>
          </v-card>
        </v-col>
      </v-row>
    </v-layout>
  </v-container>
</template>
<script>
function handleErrors(response) {
  if (!response.ok) {
    throw Error(response.statusText);
  }
  return response;
}

export default {
  name: 'ExtensionList',
  data() {
    return {
      tab: null,
      extensions: [],
      locations: [],
      deleteDialog: false,
      locationsDialog: false,
      locationsDialogDelete: false,
      headers: [
        {
          text: 'Name',
          align: 'start',
          value: 'name'
        },
        { text: 'Type', value: 'type' },
        { text: 'Location', value: 'location' },
        { text: 'Actions', value: 'actions', sortable: false }
      ],
      locationsTypes: ['URL', 'GithubRelease'],
      locationsEditedIndex: -1,
      locationsEditedItem: {
        name: '',
        type: 'URL',
        location: ''
      },
      locationsDefaultItem: {
        name: '',
        type: 'URL',
        location: ''
      }
    };
  },
  computed: {
    installedExtensions: function() {
      return this.extensions.filter(function(ext) {
        console.log('ext id ' + ext.id);
        console.log('ext installed ' + ext.installed);
        return ext.installed === true;
      });
    },
    availableExtensions: function() {
      return this.extensions.filter(function(ext) {
        return ext.installed !== true;
      });
    },
    locationsFormTitle() {
      return this.editedIndex === -1 ? 'New Item' : 'Edit Item';
    }
  },
  watch: {
    locationsDialog(val) {
      val || this.locationsClose();
    },
    locationsDialogDelete(val) {
      val || this.locationsCloseDelete();
    }
  },

  methods: {
    loadExtensions: function(doRefresh) {
      fetch(`/api/extensions?refresh=${doRefresh}`)
        .then(response => response.json())
        .then(data => {
          if (typeof data !== 'undefined' && data != null) {
            this.extensions = data;
          }
        });
    },

    loadExtensionLocations: function() {
      fetch('/api/extension_locations')
        .then(response => response.json())
        .then(data => {
          if (typeof data !== 'undefined' && data != null) {
            this.locations = data;
          }
        });
    },

    updateExtension: function(extensionId) {
      var url = `/api/extensions/${extensionId}?action=update`;
      //TODO: handle response
      fetch(url, {
        method: 'POST',
        body: null
      })
        .then(handleErrors)
        .then(response => {
          this.loadExtensions(true);
        });
    },

    downloadExtension: function(extensionId) {
      var url = `/api/extensions/${extensionId}?action=download`;
      //TODO: handle response
      fetch(url, {
        method: 'POST',
        body: null
      })
        .then(handleErrors)
        .then(response => {
          this.loadExtensions(true);
        });
    },

    deleteExtension: function(extensionId) {
      var url = `/api/extensions/${extensionId}`;
      //TODO: handle response
      fetch(url, {
        method: 'DELETE',
        body: null
      })
        .then(handleErrors)
        .then(response => {
          this.deleteDialog = false;
          this.loadExtensions(true);
        });
    },

    locationsEditItem(item) {
      this.locationsEditedIndex = this.locations.indexOf(item);
      this.locationsEditedItem = Object.assign({}, item);
      this.locationsDialog = true;
    },

    locationsDeleteItem(item) {
      this.locationsEditedIndex = this.locations.indexOf(item);
      this.locationsEditedItem = Object.assign({}, item);
      this.locationsDialogDelete = true;
    },

    locationsDeleteItemConfirm() {
      this.locations.splice(this.locationsEditedIndex, 1);

      var url = `/api/extension_locations/${this.locationsEditedItem.id}`;
      //TODO: handle response
      fetch(url, {
        method: 'DELETE',
        body: null
      })
        .then(handleErrors)
        .then(response => {
          this.loadExtensionLocations();
          this.loadExtensions(false);
        });
      this.locationsCloseDelete();
    },

    locationsClose() {
      this.locationsDialog = false;
      this.$nextTick(() => {
        this.locationsEditedItem = Object.assign({}, this.locationsDefaultItem);
        this.locationsEditedIndex = -1;
      });
    },

    locationsCloseDelete() {
      this.locationsDialogDelete = false;
      this.$nextTick(() => {
        this.locationsEditedItem = Object.assign({}, this.locationsDefaultItem);
        this.locationsEditedIndex = -1;
      });
    },

    locationsSave() {
      var url;
      var method;
      var body = JSON.stringify(this.locationsEditedItem);
      if (this.locationsEditedIndex > -1) {
        Object.assign(
          this.locations[this.locationsEditedIndex],
          this.locationsEditedItem
        );
        url = `/api/extension_locations/${this.locationsEditedItem.id}`;
        method = 'PATCH';
      } else {
        this.locations.push(this.locationsEditedItem);
        url = `/api/extension_locations`;
        method = 'POST';
      }
      //TODO: handle response
      fetch(url, {
        method: method,
        body: body
      })
        .then(handleErrors)
        .then(response => {
          this.loadExtensionLocations();
          this.loadExtensions(false);
        });

      this.locationsClose();
    }
  },
  mounted: function() {
    this.loadExtensions(false);
    this.loadExtensionLocations();
  }
};
</script>
<style scoped></style>
